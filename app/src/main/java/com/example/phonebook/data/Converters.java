package com.example.phonebook.data;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Converters {
    private static final String TAG = Converters.class.getSimpleName();
    @SuppressLint("SimpleDateFormat")
    private final static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    @TypeConverter
    public static Date intToDate(Integer n) {
        try {
            return n == null ? null : format.parse(n.toString());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dob from database", e);
            return null;
        }
    }

    @TypeConverter
    public static Integer dateToInt(Date d) {
        return d == null ? null : Integer.parseInt(format.format(d));
    }
}
