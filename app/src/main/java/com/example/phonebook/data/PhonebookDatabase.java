package com.example.phonebook.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Contact.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class PhonebookDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();

    private static volatile PhonebookDatabase INSTANCE;

    private static Callback populateOnCreate(Consumer<ContactDao> populate) {
        return new Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                populate.accept(INSTANCE.contactDao());
            }
        };
    }

    static PhonebookDatabase getInstance(final Context context,
                                         final Consumer<ContactDao> populate) {
        if (INSTANCE == null) {
            synchronized (PhonebookDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context,
                            PhonebookDatabase.class, "phonebook.db")
                            .addCallback(populateOnCreate(populate))
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
