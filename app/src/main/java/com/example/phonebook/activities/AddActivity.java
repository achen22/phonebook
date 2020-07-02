package com.example.phonebook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.lifecycle.ViewModelProvider;

import com.example.phonebook.R;
import com.example.phonebook.viewmodels.ContactViewModel;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class AddActivity extends BaseChildActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildView(R.layout.activity_add);
        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        final TextInputLayout nameLayout = findViewById(R.id.add_name_layout);
        final EditText nameField = findViewById(R.id.add_name_field);
        nameField.setText(viewModel.getName());
        nameField.addTextChangedListener((OnTextChangedWatcher) (s, start, before, count) -> {
            if (s.length() != 0) {
                nameLayout.setError(null);
            }
            viewModel.setName(s.toString());
        });
        nameField.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && nameField.getText().length() == 0) {
                nameLayout.setError(getText(R.string.name_cannot_be_empty_error));
            }
        });

        EditText emailField = findViewById(R.id.add_email_field);
        emailField.setText(viewModel.getEmail());
        emailField.addTextChangedListener((OnTextChangedWatcher) (s, start, before, count) -> {
            viewModel.setEmail(s.toString());
        });

        EditText phoneField = findViewById(R.id.add_phone_field);
        phoneField.setText(viewModel.getPhone());
        phoneField.addTextChangedListener((OnTextChangedWatcher) (s, start, before, count) -> {
            viewModel.setPhone(s.toString());
        });

        final TextInputLayout dobLayout = findViewById(R.id.add_dob_layout);
        final EditText dobField = findViewById(R.id.add_dob_field);
        if (viewModel.getCalendar() != null) {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            dobField.setText(dateFormat.format(viewModel.getCalendar().getTime()));
        }
        dobLayout.setEndIconOnClickListener(v -> {
            dobField.setText(null);
            viewModel.clearCalendar();
        });
        dobField.setOnClickListener(v -> showDatePickerDialog(dobField, viewModel.getCalendar()));

        View btnAdd = findViewById(R.id.add_layout_btn);
        btnAdd.setOnClickListener(view -> {
            if (nameField.getText().length() != 0) {
                Intent data = new Intent().putExtra("contact", viewModel.getContact());
                setResult(RESULT_OK, data);
                finish();
            } else {
                nameLayout.setError(getText(R.string.name_cannot_be_empty_error));
                nameLayout.requestFocus();
            }
        });
    }
}
