package com.example.phonebook.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.phonebook.R;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private boolean useDarkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeFromSharedPrefs();
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.appbar_main));

        // TODO: link to appropriate floating action button
        View addBtn = findViewById(R.id.btn_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddActivity.class);
                startActivity(intent);
            }
        });

        // TODO: move this to RecyclerView.Adapter?
        View editBtn = findViewById(R.id.btn_edit);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditActivity.class);
                // TODO: use the appropriate id
                intent.putExtra("id", 5);
                startActivity(intent);
            }
        });

        // TODO: move this to RecyclerView.Adapter?
        View detailBtn = findViewById(R.id.btn_detail);
        detailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                // TODO: use the appropriate id
                intent.putExtra("id", 5);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        SharedPreferences preferences = getSharedPreferences(
                getApplicationContext().getPackageName(), MODE_PRIVATE);
        if (preferences.getBoolean(WelcomeActivity.DARK_THEME_KEY, false) != useDarkTheme) {
            recreate();
        }
        super.onResume();
    }

    private void setThemeFromSharedPrefs() {
        SharedPreferences preferences = getSharedPreferences(
                getApplicationContext().getPackageName(), MODE_PRIVATE);
        if (preferences.contains(WelcomeActivity.DARK_THEME_KEY)) {
            applyTheme(preferences);
        } else {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }
    }

    private void applyTheme(@NonNull SharedPreferences preferences) {
        useDarkTheme = preferences.getBoolean(WelcomeActivity.DARK_THEME_KEY, false);
        setTheme(useDarkTheme ? R.style.DarkTheme : R.style.AppTheme);
    }
}
