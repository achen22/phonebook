package com.example.phonebook.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class PhonebookRepository {
    private static final String DEFAULT_ERROR_MESSAGE = "Connection error";
    private static final String LAST_SYNC = "lastSync";
    private static final String ONLINE_MODE = "syncChanges";

    private static volatile PhonebookRepository INSTANCE;
    private ContactDao contactDao;
    private Contact oldContact;
    private PhonebookRemote remote;
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

    // must be run on UI thread
    private void postSyncMessage(@NonNull String message) {
        syncMessage.setValue(null);
        syncMessage.postValue(message);
    }

    private void sync(ContactDao dao) {
        if (!remote.isConnected()) {
            setOnlineMode(false);
            postSyncMessage("No internet connection");
        }

        Consumer<List<Contact>> onSuccess = list -> {
            AsyncTask.execute(() -> {
                // TODO: push offline changes
                dao.replaceAll(list);
                setOnlineMode(true);
                // TODO: update last sync time in SharedPrefs
            });
            postSyncMessage("");
        };

        Consumer<Response<?>> onFailure = response -> {
            setOnlineMode(false);
            postSyncMessage(response == null ? DEFAULT_ERROR_MESSAGE : response.message());
        };

        remote.all(onSuccess, onFailure);
    }

    public void sync() {
        sync(contactDao);
    }

    private void setOnlineMode(boolean onlineMode) {
        this.onlineMode = onlineMode;
        preferences.edit().putBoolean(ONLINE_MODE, onlineMode).apply();
    }
}
