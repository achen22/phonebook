package com.example.phonebook.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.phonebook.R;

public class AddActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(TAG);
        }
    }
}
