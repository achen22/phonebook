package com.example.phonebook.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.phonebook.R;

public class EditActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(TAG);
        }
    }
}
