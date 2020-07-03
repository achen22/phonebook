package com.example.phonebook.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class PhonebookRepository {
    private static final String DEFAULT_ERROR_MESSAGE = "Sync failed: Connection error";
    private static final String LAST_SYNC = "lastSync";
    private static final String SYNC_STATE = "syncState";

    private static volatile PhonebookRepository INSTANCE;
    private ContactDao contactDao;
    private Contact oldContact;
    private PhonebookRemote remote;
    private final SharedPreferences preferences;

    private MutableLiveData<Boolean> syncState;
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
        syncState = new MutableLiveData<>(preferences.getBoolean(SYNC_STATE, false));
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
    public void save(final Contact contact) {
        AsyncTask.execute(() -> {
            if (contact.getId() == -1) {
                // Insert new contact
                long id = contactDao.maxId() + 1;
                oldContact = new Contact(id);
                contact.setId(id);
                contactDao.insert(contact);
                // TODO: try to get id assigned via API, otherwise add this to backlog of contacts to add
            } else {
                // Update existing contact
                oldContact = contactDao.select(contact.getId());
                if (contact.equals(oldContact)) {
                    oldContact = null;
                    return;
                }
                contactDao.update(contact);
                // TODO: try to update via API, otherwise add this to backlog of contacts to update
            }
        });
    }

    public void delete(final long id) {
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

    private void sync(ContactDao dao) {
        Consumer<List<Contact>> onSuccess = list -> {
            AsyncTask.execute(() -> {
                // TODO: push offline changes
                dao.replaceAll(list);
                setSyncState(true);
                // TODO: update last sync time in SharedPrefs
            });
        };

        Consumer<Response<?>> onFailure = response -> {
            setSyncState(false);
            syncMessage.postValue(response == null ? DEFAULT_ERROR_MESSAGE : response.message());
        };

        remote.all(onSuccess, onFailure);
    }

    public void sync() {
        sync(contactDao);
    }

    private void setSyncState(boolean synced) {
        syncState.postValue(synced);
        preferences.edit().putBoolean(SYNC_STATE, synced).apply();
    }
}
