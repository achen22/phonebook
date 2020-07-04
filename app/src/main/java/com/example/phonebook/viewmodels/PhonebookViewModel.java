package com.example.phonebook.viewmodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phonebook.data.Contact;
import com.example.phonebook.data.ContactsHashTable;
import com.example.phonebook.data.PhonebookRepository;

import java.util.List;

public class PhonebookViewModel extends ViewModel {
    private PhonebookRepository repository;
    private LiveData<List<Contact>> phonebook;
    private ContactsHashTable hashTable;
    private boolean reverse = false;
    @NonNull private String search = "";
    private MediatorLiveData<ContactsHashTable> contacts = new MediatorLiveData<>();
    private boolean nextMessageVisible = false;

    public LiveData<ContactsHashTable> getContacts(Context context) {
        if (repository == null) {
            repository = PhonebookRepository.getInstance(context);
            phonebook = repository.all();
            contacts.addSource(phonebook, list -> {
                if (list != null) {
                    hashTable = new ContactsHashTable(list, reverse);
                    updateContactsLiveData();
                }
            });
        }
        return contacts;
    }

    public void setReverse (boolean reverse) {
        if (this.reverse != reverse) {
            this.reverse = reverse;
            hashTable = new ContactsHashTable(phonebook.getValue(), reverse);
            updateContactsLiveData();
        }
    }

    public void setSearchString(@NonNull String search) {
        if (!this.search.equalsIgnoreCase(search)) {
            this.search = search;
            updateContactsLiveData();
        }
    }

    @NonNull public String getSearchString() {
        return search;
    }

    private void updateContactsLiveData() {
        if (search.isEmpty()) {
            contacts.postValue(hashTable);
        } else {
            contacts.postValue(new ContactsHashTable(hashTable.searchName(search), reverse));
        }
    }

    public void save(Contact contact) {
        repository.save(contact);
    }

    public Runnable delete(long id) {
        return repository.delete(id);
    }

    public Runnable delete(Contact contact) {
        return repository.delete(contact);
    }

    public void undoSave() {
        repository.undoSave();
    }

    public void undoDelete() {
        repository.undoDelete();
    }

    public void sync() {
        repository.sync();
        nextMessageVisible = true;
    }

    public boolean isNextMessageVisible() {
        return nextMessageVisible;
    }

    public void consumeMessage() {
        nextMessageVisible = false;
    }

    public LiveData<String> getSyncMessage() {
        return repository.getSyncMessage();
    }
}
