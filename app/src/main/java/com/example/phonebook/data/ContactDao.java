package com.example.phonebook.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM Contact ORDER BY id")
    LiveData<List<Contact>> all();

    @Query("SELECT * FROM Contact WHERE id = :id")
    LiveData<Contact> get(long id);

    @Query("SELECT * FROM Contact WHERE id = :id")
    Contact select(long id);

    @Insert void insert(Contact... contacts);
    @Update void update(Contact... contacts);
    @Delete void delete(Contact... contacts);

    @Query("UPDATE Contact SET id = :newId WHERE id = :oldId")
    void updateId(long oldId, long newId);

    @Query("SELECT MAX(id) FROM Contact")
    long maxId();
}
