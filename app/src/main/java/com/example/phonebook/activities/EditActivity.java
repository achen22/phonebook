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

public class EditActivity extends BaseChildActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildView(R.layout.activity_edit);
        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        viewModel.load(getIntent().getLongExtra("id", 0));

        final TextInputLayout nameLayout = findViewById(R.id.edit_name_layout);
        final EditText nameField = findViewById(R.id.edit_name_field);
        nameField.setText(viewModel.getName());
        nameField.addTextChangedListener(new OnTextChangedWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    nameLayout.setError(null);
                }
                viewModel.setName(s.toString());
            }
        });
        nameField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && nameField.getText().length() == 0) {
                    nameLayout.setError(getText(R.string.name_cannot_be_empty_error));
                }
            }
        });

        EditText emailField = findViewById(R.id.edit_email_field);
        emailField.setText(viewModel.getEmail());
        emailField.addTextChangedListener(new OnTextChangedWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setEmail(s.toString());
            }
        });

        EditText phoneField = findViewById(R.id.edit_phone_field);
        phoneField.setText(viewModel.getPhone());
        phoneField.addTextChangedListener(new OnTextChangedWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setPhone(s.toString());
            }
        });

        final TextInputLayout dobLayout = findViewById(R.id.edit_dob_layout);
        final EditText dobField = findViewById(R.id.edit_dob_field);
        if (viewModel.getCalendar() != null) {
            DateFormat dateFormat = SimpleDateFormat.getDateInstance();
            dobField.setText(dateFormat.format(viewModel.getCalendar().getTime()));
        }
        dobLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dobField.setText(null);
                viewModel.clearCalendar();
            }
        });
        dobField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(dobField, viewModel.getCalendar());
            }
        });

        View btnEdit = findViewById(R.id.edit_layout_btn);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameField.getText().length() != 0) {
                    Intent data = new Intent().putExtra("contact", viewModel.getContact());
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    nameLayout.setError(getText(R.string.name_cannot_be_empty_error));
                    nameLayout.requestFocus();
                }
            }
        });
    }
}
