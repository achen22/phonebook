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
    private Consumer<String> onFailure = null;

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
            if (onFailure != null) {
                onFailure.accept(errorMessage);
                onFailure = null;
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
                preferences.edit().remove(DELETED_IDS).remove(CHANGED_IDS).apply();

                dao.replaceAll(list);
                setOnlineMode(true);

                long now = Calendar.getInstance().getTimeInMillis();
                preferences.edit().putLong(LAST_SYNC, now).apply();
            });
            postSyncMessage(null);
        };
        onFailure = this::postSyncMessage;
        remote.all(onSuccess);
    }

    /**
     * Saves changes made to a new or existing contact. If online mode if active, this method
     * returns a Runnable to push the changes to the remote database.
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

            addToBacklog(CHANGED_IDS, id);
            if (!preferences.contains(LAST_SYNC_ID)) {
                preferences.edit().putLong(LAST_SYNC_ID, id - 1).apply();
            }

            if (onlineMode) {
                insertRemote(contact);
            }
        });
    }

    private void insertRemote(Contact contact) {
        if (!onlineMode || !backlogContains(CHANGED_IDS, oldContact.getId())) {
            onlineMode = false;
            return;
        }

        long id = contact.getId();
        Consumer<Contact> onSuccess = response -> {
            onFailure = null;

            AsyncTask.execute(() -> {
                contactDao.updateId(id, response.getId());
                preferences.edit().remove(LAST_SYNC_ID).apply();
                removeFromBacklog(CHANGED_IDS, id);

                // notify others of updated id
                oldContact.setId(response.getId());
                if (backlogContains(DELETED_IDS, id)) {
                    removeFromBacklog(DELETED_IDS, id);
                    addToBacklog(DELETED_IDS, response.getId());
                }
            });
        };
        onFailure = message -> {
            setOnlineMode(false);
        };
        remote.insert(contact, onSuccess);
    }

    private void update(Contact contact) {
        addToBacklog(CHANGED_IDS, contact.getId());
        AsyncTask.execute(() -> {
            oldContact = contactDao.select(contact.getId());
            if (contact.equals(oldContact)) {
                oldContact = null;
                return;
            }
            contactDao.update(contact);

            if (!preferences.contains(LAST_SYNC_ID)) {
                preferences.edit().putLong(LAST_SYNC_ID, contactDao.maxId()).apply();
            }

            if (onlineMode) {
                updateRemote(contact);
            }
        });
    }

    private void updateRemote(Contact contact) {
        if (!onlineMode || !backlogContains(CHANGED_IDS, oldContact.getId())) {
            return;
        }

        Runnable onSuccess = () -> {
            onFailure = null;
            preferences.edit().remove(LAST_SYNC_ID).apply();
            removeFromBacklog(CHANGED_IDS, contact.getId());
        };
        Runnable onNotFound = () -> {
            insertRemote(contact);
        };
        onFailure = message -> {
            setOnlineMode(false);
        };
        remote.update(contact, onSuccess, onNotFound);
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
        addToBacklog(DELETED_IDS, id);
        AsyncTask.execute(() -> {
            oldContact = contactDao.select(id);
            if (oldContact != null) {
                if (!preferences.contains(LAST_SYNC_ID)) {
                    preferences.edit().putLong(LAST_SYNC_ID, contactDao.maxId()).apply();
                }
                contactDao.delete(oldContact);
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
        if (!onlineMode || !backlogContains(DELETED_IDS, oldContact.getId())) {
            return;
        }

        Consumer<Contact> onSuccess = contact -> {
            onFailure = null;
            preferences.edit().remove(LAST_SYNC_ID).apply();
            removeFromBacklog(DELETED_IDS, id);
        };
        onFailure = message -> {
            setOnlineMode(false);
        };
        remote.delete(id, onSuccess);
    }

    public void undoSave() {
        if (oldContact == null) {
            return;
        }
        long id = oldContact.getId();
        AsyncTask.execute(() -> {
            if (oldContact.getName().isEmpty()) {
                // delete added contact
                removeFromBacklog(CHANGED_IDS, id);
                Runnable remoteDelete = delete(id);
                if (remoteDelete != null) {
                    remoteDelete.run();
                }
            } else {
                // undo updated contact
                update(oldContact);
            }
        });
    }

    public void undoDelete() {
        if (oldContact == null || !backlogContains(DELETED_IDS, oldContact.getId())) {
            return;
        }
        AsyncTask.execute(() -> {
            contactDao.insert(oldContact);
            removeFromBacklog(DELETED_IDS, oldContact.getId());
        });
    }

    private void addToBacklog(String backlog, long id) {
        Set<String> strings = preferences.getStringSet(backlog, new HashSet<>());
        if (strings == null) {
            strings = new HashSet<>();
        }
        strings.add(String.valueOf(id));
        preferences.edit().putStringSet(backlog, strings).apply();
    }

    private void removeFromBacklog(String backlog, long id) {
        Set<String> strings = preferences.getStringSet(backlog, new HashSet<>());
        if (strings != null && strings.remove(String.valueOf(id))) {
            preferences.edit().putStringSet(backlog, strings).apply();
        }
    }

    private boolean backlogContains(String backlog, long id) {
        Set<String> strings = preferences.getStringSet(backlog, null);
        return strings != null && strings.contains(String.valueOf(id));
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
        } else if (!preferences.contains(LAST_SYNC_ID)) {
            AsyncTask.execute(() -> {
                long id = contactDao.maxId();
                if (!preferences.contains(LAST_SYNC_ID)) {
                    preferences.edit().putLong(LAST_SYNC_ID, id).apply();
                }
            });
        }
    }
}
