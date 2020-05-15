package com.example.phonebook.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.phonebook.data.Contact;
import com.example.phonebook.data.PhonebookRepository;

import java.util.List;

public class PhonebookViewModel extends ViewModel {
    private PhonebookRepository repository;
    private LiveData<List<Contact>> phonebook;

    public LiveData<List<Contact>> getContacts() {
        if (repository == null) {
            repository = PhonebookRepository.getInstance();
            phonebook = repository.all();
        }
        return phonebook;
    }

    public String getName(long id) {
        Contact contact = repository.get(id);
        return contact.getName();
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
