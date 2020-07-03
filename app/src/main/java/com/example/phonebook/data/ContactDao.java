package com.example.phonebook.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class ContactDao {
    @Query("SELECT * FROM Contact ORDER BY id")
    abstract LiveData<List<Contact>> all();

    @Query("SELECT * FROM Contact WHERE id = :id")
    abstract LiveData<Contact> get(long id);

    @Query("SELECT * FROM Contact WHERE id = :id")
    abstract Contact select(long id);

    @Insert abstract void insert(Contact... contacts);
    @Update abstract void update(Contact... contacts);
    @Delete abstract void delete(Contact... contacts);

    @Query("DELETE FROM Contact")
    abstract void deleteAll();

    @Transaction
    void replaceAll(List<Contact> contacts) {
        deleteAll();
        insert(contacts.toArray(new Contact[0]));
    }

    @Query("UPDATE Contact SET id = :newId WHERE id = :oldId")
    abstract void updateId(long oldId, long newId);

    @Query("SELECT MAX(id) FROM Contact")
    abstract long maxId();
}
