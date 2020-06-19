package com.example.phonebook.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Contact.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class PhonebookDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();

    private static volatile PhonebookDatabase INSTANCE;

    static PhonebookDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (PhonebookDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                            PhonebookDatabase.class, "phonebook.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
