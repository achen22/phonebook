package com.example.phonebook.data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class PhonebookRepository {
    private static volatile PhonebookRepository INSTANCE;
    private static ContactDao contactDao;
    private static Contact oldContact;
    private static ContactEndpoint endpoint;
    private static MutableLiveData<Boolean> syncState;

    public static PhonebookRepository getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (PhonebookDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PhonebookRepository();
                    PhonebookDatabase phonebookDatabase = PhonebookDatabase.getInstance(context);
                    contactDao = phonebookDatabase.contactDao();
                    PhonebookRemote remote = PhonebookRemote.getInstance();
                    endpoint = remote.getEndpoint();
                }
            }
        }
        return INSTANCE;
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
}
