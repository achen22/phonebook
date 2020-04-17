package com.example.phonebook.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.phonebook.R;

public class WelcomeActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    public static final String DARK_THEME_KEY = "useDarkTheme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        setContentView(R.layout.activity_welcome);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button btnLight = findViewById(R.id.btn_light_theme);
        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPreferences(MODE_PRIVATE).edit()
                        .putBoolean(DARK_THEME_KEY, false)
                        .apply();
                recreate();
            }
        });

        Button btnDark = findViewById(R.id.btn_dark_theme);
        btnDark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPreferences(MODE_PRIVATE).edit()
                        .putBoolean(DARK_THEME_KEY, true)
                        .apply();
                recreate();
            }
        });
    }

    private void applyTheme() {
        int styleId = getPreferences(MODE_PRIVATE).getBoolean(DARK_THEME_KEY, false)
                ? R.style.DarkTheme
                : R.style.AppTheme;
        setTheme(styleId);
    }

    @Override
    public void onBackPressed() {
        // back out of app
        if (!moveTaskToBack(true)) {
            Intent main = new Intent(Intent.ACTION_MAIN);
            main.addCategory(Intent.CATEGORY_HOME);
            main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(main);
        }
    }
}
