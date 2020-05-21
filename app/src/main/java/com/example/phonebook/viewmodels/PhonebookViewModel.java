package com.example.phonebook.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.phonebook.data.Contact;
import com.example.phonebook.data.ContactsHashTable;
import com.example.phonebook.data.PhonebookRepository;

import java.util.List;

public class PhonebookViewModel extends ViewModel {
    private PhonebookRepository repository;
    private LiveData<List<Contact>> phonebook;
    private boolean reverse = false;
    private MediatorLiveData<ContactsHashTable> contacts = new MediatorLiveData<>();

    public LiveData<ContactsHashTable> getContacts() {
        if (repository == null) {
            repository = PhonebookRepository.getInstance();
            phonebook = repository.all();
            contacts.addSource(phonebook, new Observer<List<Contact>>() {
                @Override
                public void onChanged(List<Contact> list) {
                    if (list != null) {
                        contacts.postValue(new ContactsHashTable(list, reverse));
                    }
                }
            });
        }
        return contacts;
    }

    public String getName(long id) {
        Contact contact = repository.get(id);
        return contact.getName();
    }

    public void setReverse (boolean reverse) {
        if (this.reverse != reverse) {
            this.reverse = reverse;
            contacts.postValue(new ContactsHashTable(phonebook.getValue(), reverse));
        }
    }

    public void save(Contact contact) {
        repository.save(contact);
    }

    public void delete(long id) {
        repository.delete(id);
    }

    public void delete(Contact contact) {
        repository.delete(contact);
    }

    public void undoSave() {
        repository.undoSave();
    }

    public void undoDelete() {
        repository.undoDelete();
    }
}
