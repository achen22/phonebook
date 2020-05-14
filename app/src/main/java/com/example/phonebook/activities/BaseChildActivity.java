package com.example.phonebook.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.phonebook.R;
import com.example.phonebook.viewmodels.ContactViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class BaseChildActivity extends AppCompatActivity {
    protected final String TAG = this.getClass().getSimpleName();
    protected ContactViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
        applyChildActionBar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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

    protected void showDatePickerDialog(@NonNull final EditText editText, Calendar calendar) {
        final Calendar date = calendar != null ? calendar : Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(editText.getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        date.set(year, month, day);
                        DateFormat format = SimpleDateFormat.getDateInstance();
                        editText.setText(format.format(date.getTime()));
                        viewModel.setCalendar(year, month, day);
                    }
                },
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    protected abstract static class OnTextChangedWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
