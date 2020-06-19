package com.example.phonebook.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.phonebook.data.Contact;
import com.example.phonebook.data.PhonebookRepository;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ContactViewModel extends ViewModel {
    private Contact contact = new Contact(-1);
    private Calendar calendar = null;

    public LiveData<Contact> load(Context context, long id) {
        if (id == -1 || contact.getId() == id) {
            return null;
        }
        return PhonebookRepository.getInstance(context).get(id);
    }

    public void load(Contact contact) {
        this.contact.setId(contact.getId());
        setName(contact.getName());
        setEmail(contact.getEmail());
        setPhone(contact.getPhone());
        if (contact.getDob() != null) {
            calendar = new GregorianCalendar();
            calendar.clear();
            calendar.setTime(contact.getDob());
        }
    }

    public Contact getContact() {
        contact.setDob(calendar != null ? calendar.getTime() : null);
        if (getEmail() != null && getEmail().isEmpty()) {
            setEmail(null);
        }
        if (getPhone() != null && getPhone().isEmpty()) {
            setPhone(null);
        }
        return contact;
    }

    public String getName() {
        return contact.getName();
    }

    public void setName(String name) {
        contact.setName(name);
    }

    public String getEmail() {
        return contact.getEmail();
    }

    public void setEmail(String email) {
        contact.setEmail(email);
    }

    public String getPhone() {
        return contact.getPhone();
    }

    public void setPhone(String phone) {
        contact.setPhone(phone);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(int year, int month, int date) {
        calendar = new GregorianCalendar(year, month, date);
    }

    public void clearCalendar() {
        calendar = null;
    }
}
