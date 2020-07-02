package com.example.phonebook.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.phonebook.R;
import com.example.phonebook.data.Contact;
import com.example.phonebook.viewmodels.ContactViewModel;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditActivity extends BaseChildActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildView(R.layout.activity_edit);
        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        long id = getIntent().getLongExtra("id", 0);
        final LiveData<Contact> liveData = viewModel.load(getApplicationContext(), id);
        if (liveData != null) {
            liveData.observe(this, new Observer<Contact>() {
                @Override
                public void onChanged(Contact contact) {
                    liveData.removeObserver(this);
                    viewModel.load(contact);
                    fillInputFields();
                }
            });
        }

        final TextInputLayout nameLayout = findViewById(R.id.edit_name_layout);
        final EditText nameField = findViewById(R.id.edit_name_field);
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

        EditText emailField = findViewById(R.id.edit_email_field);
        emailField.setText(viewModel.getEmail());
        emailField.addTextChangedListener((OnTextChangedWatcher) (s, start, before, count) -> {
            viewModel.setEmail(s.toString());
        });

        EditText phoneField = findViewById(R.id.edit_phone_field);
        phoneField.setText(viewModel.getPhone());
        phoneField.addTextChangedListener((OnTextChangedWatcher) (s, start, before, count) -> {
            viewModel.setPhone(s.toString());
        });

        final TextInputLayout dobLayout = findViewById(R.id.edit_dob_layout);
        final EditText dobField = findViewById(R.id.edit_dob_field);
        if (viewModel.getCalendar() != null) {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            dobField.setText(dateFormat.format(viewModel.getCalendar().getTime()));
        }
        dobLayout.setEndIconOnClickListener(v -> {
            dobField.setText(null);
            viewModel.clearCalendar();
        });
        dobField.setOnClickListener(v -> {
            showDatePickerDialog(dobField, viewModel.getCalendar());
        });

        View btnEdit = findViewById(R.id.edit_layout_btn);
        btnEdit.setOnClickListener(view -> {
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

    private void fillInputFields() {
        EditText nameField = findViewById(R.id.edit_name_field);
        nameField.setText(viewModel.getName());

        EditText emailField = findViewById(R.id.edit_email_field);
        emailField.setText(viewModel.getEmail());

        EditText phoneField = findViewById(R.id.edit_phone_field);
        phoneField.setText(viewModel.getPhone());

        Calendar calendar = viewModel.getCalendar();
        if (calendar != null) {
            EditText dobField = findViewById(R.id.edit_dob_field);
            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            dobField.setText(dateFormat.format(calendar.getTime()));
        }
    }
}
