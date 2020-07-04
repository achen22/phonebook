package com.example.phonebook.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class PhonebookRepository {
    private static final String LAST_SYNC = "lastSync";
    private static final String LAST_SYNC_ID = "addAbove";
    private static final String CHANGED_IDS = "changes";
    private static final String DELETED_IDS = "deletes";

    private static volatile PhonebookRepository INSTANCE;
    private ContactDao contactDao;
    private Contact oldContact;

    private final PhonebookRemote remote;
    /** This gets called when an error occurs during a remote operation  */
    private Consumer<String> remoteErrorConsumer = null;

    private final SharedPreferences preferences;
    private boolean onlineMode;
    private MutableLiveData<String> syncMessage = new MutableLiveData<>();

    public static PhonebookRepository getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (PhonebookDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PhonebookRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    private PhonebookRepository(Context context) {
        remote = PhonebookRemote.getInstance(context);
        remote.getMessage().observeForever(errorMessage -> {
            setOnlineMode(false);
            if (remoteErrorConsumer != null) {
                Consumer<String> consumer = remoteErrorConsumer;
                remoteErrorConsumer = null;
                consumer.accept(errorMessage);
            }
        });

        PhonebookDatabase phonebookDatabase = PhonebookDatabase.getInstance(context, this::sync);
        contactDao = phonebookDatabase.contactDao();

        preferences = context.getSharedPreferences(
                context.getApplicationContext().getPackageName(),
                MODE_PRIVATE);
        onlineMode = preferences.contains(LAST_SYNC_ID);
    }

    public LiveData<List<Contact>> all() {
        return contactDao.all();
    }

    public LiveData<Contact> get(long id) {
        return contactDao.get(id);
    }

    public void sync() {
        sync(contactDao);
    }

    private void sync(ContactDao dao) {
        Consumer<List<Contact>> onSuccess = list -> {
            AsyncTask.execute(() -> {
                // TODO: push offline changes
                dao.replaceAll(list);
                setOnlineMode(true);

                long now = Calendar.getInstance().getTimeInMillis();
                preferences.edit().putLong(LAST_SYNC, now).apply();
            });
            postSyncMessage(null);
        };

        remoteErrorConsumer = this::postSyncMessage;
        remote.all(onSuccess);
    }

    /**
     * Saves changes made to a new or existing contact.
     * @param contact The contact to be saved
     */
    public void save(Contact contact) {
        if (contact.getId() == -1) {
            insert(contact);
        } else {
            update(contact);
        }
    }

    private void insert(Contact contact) {
        AsyncTask.execute(() -> {
            long id = contactDao.maxId() + 1;
            oldContact = new Contact(id);
            contact.setId(id);
            contactDao.insert(contact);
            // TODO: try to get id assigned via API, otherwise add this to backlog of contacts to add
        });
    }

    private void update(Contact contact) {
        AsyncTask.execute(() -> {
            oldContact = contactDao.select(contact.getId());
            if (contact.equals(oldContact)) {
                oldContact = null;
                return;
            }
            contactDao.update(contact);
            // TODO: try to update via API, otherwise add this to backlog of contacts to update
        });
    }

    /**
     * Deletes a contact from the local database. If online mode is active, this method returns a
     * Runnable to delete the contact from the remote database.
     * @param contact the contact to be deleted
     * @return a Runnable to delete the contact from the remote database if online mode is active,
     * null otherwise
     */
    public Runnable delete(Contact contact) {
        return delete(contact.getId());
    }

    /**
     * Deletes a contact from the local database. If online mode is active, this method returns a
     * Runnable to delete the contact from the remote database.
     * @param id the ID of the contact to be deleted
     * @return a Runnable to delete the contact from the remote database if online mode is active,
     * null otherwise
     */
    public Runnable delete(long id) {
        AsyncTask.execute(() -> {
            oldContact = contactDao.select(id);
            if (oldContact != null) {
                contactDao.delete(oldContact);
                deleteLater(id);
            }
        });

        if (!onlineMode) {
            return null;
        }
        return () -> {
            if (oldContact != null) {
                deleteRemote(id);
            }
        };
    }

    private void deleteRemote(long id) {
        if (!onlineMode) {
            return;
        }

        Consumer<Contact> onSuccess = contact -> {
            remoteErrorConsumer = null;
            removeDeleteLater(id);
        };
        remoteErrorConsumer = s -> {
            setOnlineMode(false);
        };
        remote.delete(id, onSuccess);
    }

    private void saveLater(long id) {
        Set<String> strings = preferences.getStringSet(CHANGED_IDS, new HashSet<>());
        assert strings != null;
        strings.add(String.valueOf(id));
        preferences.edit().putStringSet(CHANGED_IDS, strings).apply();
    }

    private void removeSaveLater(long id) {
        if (onlineMode) {
            preferences.edit().remove(CHANGED_IDS).apply();
            return;
        }

        Set<String> strings = preferences.getStringSet(CHANGED_IDS, new HashSet<>());
        assert strings != null;
        if (strings.remove(String.valueOf(id))) {
            preferences.edit().putStringSet(CHANGED_IDS, strings).apply();
        }
    }

    private void deleteLater(long id) {
        Set<String> strings = preferences.getStringSet(DELETED_IDS, new HashSet<>());
        assert strings != null;
        strings.add(String.valueOf(id));
        preferences.edit().putStringSet(DELETED_IDS, strings).apply();
    }

    private void removeDeleteLater(long id) {
        if (onlineMode) {
            preferences.edit().remove(DELETED_IDS).apply();
            return;
        }

        Set<String> strings = preferences.getStringSet(DELETED_IDS, new HashSet<>());
        assert strings != null;
        if (strings.remove(String.valueOf(id))) {
            preferences.edit().putStringSet(DELETED_IDS, strings).apply();
        }
    }

    public void undoSave() {
        if (oldContact == null) {
            return;
        }
        AsyncTask.execute(() -> {
            if (oldContact.getName().isEmpty()) {
                // undo added contact
                contactDao.delete(oldContact);
                // TODO: try to undo via API, otherwise add this to backlog of contacts to delete
            } else {
                contactDao.update(oldContact);
                // TODO: try to undo via API, otherwise add this to backlog of contacts to update
            }
        });
    }

    public void undoDelete() {
        if (oldContact == null) {
            return;
        }
        AsyncTask.execute(() -> {
            contactDao.insert(oldContact);
            // TODO: try to re-add via API, otherwise add this to backlog of contacts to add
        });
    }

    public LiveData<String> getSyncMessage() {
        return syncMessage;
    }

    /**
     * Sets LiveData value to null, then posts message to LiveData. Must be run on UI thread.
     * @param message the message to post to LiveData
     */
    private void postSyncMessage(String message) {
        syncMessage.setValue(null);
        syncMessage.postValue(message == null ? "" : message);
    }

    private void setOnlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode;
        if (onlineMode) {
            preferences.edit().remove(LAST_SYNC_ID).apply();
        } else {
            AsyncTask.execute(() -> {
                long id = contactDao.maxId();
                if (!preferences.contains(LAST_SYNC_ID)) {
                    preferences.edit().putLong(LAST_SYNC_ID, id).apply();
                }
            });
        }
    }
}
