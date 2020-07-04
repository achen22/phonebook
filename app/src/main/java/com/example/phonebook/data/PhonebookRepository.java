package com.example.phonebook.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PhonebookRepository {
    private static final String LAST_SYNC = "lastSync";
    private static final String ONLINE_MODE = "syncChanges";

    private static volatile PhonebookRepository INSTANCE;
    private ContactDao contactDao;
    private Contact oldContact;

    private final PhonebookRemote remote;
    /** This gets called when an error occurs during a remote operation  */
    private Consumer<String> remoteMessageConsumer = null;
    private final Consumer<String> defaultRemoteMessageConsumer = message -> {
        remoteMessageConsumer = null;
        postSyncMessage(message);
    };

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
        remote.getMessage().observeForever(message -> {
            Consumer<String> consumer = getRemoteMessageConsumer();
            if (consumer != null) {
                consumer.accept(message);
            }
        });

        PhonebookDatabase phonebookDatabase = PhonebookDatabase.getInstance(context, this::sync);
        contactDao = phonebookDatabase.contactDao();

        preferences = context.getSharedPreferences(
                context.getApplicationContext().getPackageName(),
                MODE_PRIVATE);
        onlineMode = preferences.getBoolean(ONLINE_MODE, false);
    }

    public LiveData<List<Contact>> all() {
        return contactDao.all();
    }

    public LiveData<Contact> get(long id) {
        return contactDao.get(id);
    }

    private void sync(ContactDao dao) {
        Consumer<List<Contact>> onSuccess = list -> {
            remoteMessageConsumer = null;
            AsyncTask.execute(() -> {
                // TODO: push offline changes
                dao.replaceAll(list);
                setOnlineMode(true);
                // TODO: update last sync time in SharedPrefs
            });
            postSyncMessage(null);
        };

        listenForRemoteErrorMessage();
        remote.all(onSuccess);
    }

    public void sync() {
        sync(contactDao);
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

    public void delete(long id) {
        AsyncTask.execute(() -> {
            oldContact = contactDao.select(id);
            if (oldContact != null) {
                contactDao.delete(oldContact);
                // TODO: try to delete via API, otherwise add this to backlog of contacts to delete
            }
        });
    }

    public void delete(Contact contact) {
        delete(contact.getId());
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
        AsyncTask.execute(() -> {
            contactDao.insert(oldContact);
            // TODO: try to re-add via API, otherwise add this to backlog of contacts to add
        });
    }

    public LiveData<String> getSyncMessage() {
        return syncMessage;
    }

    /** Sets LiveData value to null, then posts message to LiveData. Must be run on UI thread.
     *
     * @param message the message to post to LiveData
     */
    private void postSyncMessage(String message) {
        syncMessage.setValue(null);
        syncMessage.postValue(message == null ? "" : message);
    }

    private void setOnlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode;
        preferences.edit().putBoolean(ONLINE_MODE, onlineMode).apply();
    }

    private Consumer<String> getRemoteMessageConsumer() {
        return remoteMessageConsumer;
    }

    private void listenForRemoteErrorMessage() {
        remoteMessageConsumer = defaultRemoteMessageConsumer;
    }
}
