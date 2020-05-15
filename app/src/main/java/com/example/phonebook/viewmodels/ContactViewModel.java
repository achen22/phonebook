package com.example.phonebook.viewmodels;

import androidx.lifecycle.ViewModel;

import com.example.phonebook.data.Contact;
import com.example.phonebook.data.PhonebookRepository;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class ContactViewModel extends ViewModel {
    private Contact contact = new Contact(0);
    private Calendar calendar = null;

    public void load(long id) {
        if (id != 0 && contact.getId() != id) {
            Contact contact = PhonebookRepository.getInstance().get(id);
            this.contact.setId(contact.getId());
            setName(contact.getName());
            setEmail(contact.getEmail());
            setPhone(contact.getPhone());
            calendar = new GregorianCalendar();
            calendar.clear();
            calendar.setTime(contact.getDob());
        }
    }

    public Contact getContact() {
        contact.setDob(calendar != null ? calendar.getTime() : null);
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
