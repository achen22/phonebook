package com.example.phonebook.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonebook.R;

public class WelcomeActivity extends AppCompatActivity {
    public static final String DARK_THEME_KEY = "useDarkTheme";

    private final String TAG = this.getClass().getSimpleName();
    private SharedPreferences preferences;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(
                getApplicationContext().getPackageName(), MODE_PRIVATE);
        applyTheme();
        setContentView(R.layout.activity_welcome);
        gestureDetector = new GestureDetector(this, new WelcomeGestureListener());

        Button btnLight = findViewById(R.id.btn_light_theme);
        btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.edit()
                        .putBoolean(DARK_THEME_KEY, false)
                        .apply();
                recreate();
            }
        });

        Button btnDark = findViewById(R.id.btn_dark_theme);
        btnDark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.edit()
                        .putBoolean(DARK_THEME_KEY, true)
                        .apply();
                recreate();
            }
        });
    }

    private void applyTheme() {
        int styleId = preferences.getBoolean(DARK_THEME_KEY, false)
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class WelcomeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "WelcomeGesture";

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY() - e2.getY())) {
                // Horizontal fling
                if (!preferences.contains(DARK_THEME_KEY)) {
                    preferences.edit()
                            .putBoolean(DARK_THEME_KEY, false)
                            .apply();
                }
                finish();
                overridePendingTransition(R.anim.drift_in_right, R.anim.slide_out_left);
            }
            return true;
        }
    }
}
