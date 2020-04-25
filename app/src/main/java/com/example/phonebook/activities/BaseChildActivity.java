package com.example.phonebook.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.phonebook.R;

public abstract class BaseChildActivity extends AppCompatActivity {
    protected final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        applyChildActionBar();
    }

    private void applyTheme() {
        SharedPreferences preferences = getSharedPreferences(
                getApplicationContext().getPackageName(), MODE_PRIVATE);
        int styleId = preferences.getBoolean(WelcomeActivity.DARK_THEME_KEY, false)
                ? R.style.DarkTheme
                : R.style.AppTheme;
        setTheme(styleId);
    }

    private void applyChildActionBar() {
        setContentView(R.layout.activity_child_base);
        setSupportActionBar((Toolbar) findViewById(R.id.appbar_child));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void setChildView(int resId) {
        NestedScrollView layout = findViewById(R.id.child_base_scrollview);
        View content = getLayoutInflater().inflate(resId, layout);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
