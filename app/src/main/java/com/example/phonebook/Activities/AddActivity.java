package com.example.phonebook.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.phonebook.R;

public class AddActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(
                getApplicationContext().getPackageName(), MODE_PRIVATE);
        applyTheme(preferences);
        setContentView(R.layout.activity_add);
        setSupportActionBar((Toolbar) findViewById(R.id.appbar_add));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void applyTheme(@NonNull SharedPreferences preferences) {
        int styleId = preferences.getBoolean(WelcomeActivity.DARK_THEME_KEY, false)
                ? R.style.DarkTheme
                : R.style.AppTheme;
        setTheme(styleId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
