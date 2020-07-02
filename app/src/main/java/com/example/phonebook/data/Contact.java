package com.example.phonebook.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity
public class Contact implements Comparable<Contact>, Serializable {
    @PrimaryKey private long id;
    @NonNull private String name;
    private String email;
    private String phone;
    private Date dob;

    @Ignore
    public Contact(long id) {
        this.id = id;
        name = "";
    }

    public Contact(long id, @NonNull String name, String email, String phone, Date dob) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
    }

    @Override
    public int compareTo(Contact contact) {
        return Long.signum(id - contact.getId());
    }

    @Override
    public final boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Contact)) {
            return false;
        }
        Contact contact = (Contact) obj;
        return id == contact.id &&
                (name == contact.name || name.equals(contact.name)) &&
                (email == contact.email || email != null && email.equals(contact.email)) &&
                (phone == contact.phone || phone != null && phone.equals(contact.phone)) &&
                (dob == contact.dob || dob != null && dob.equals(contact.dob));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }
}
