package com.example.phonebook.activities;

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

        EditText nameField = findViewById(R.id.add_name_field);
        nameField.setText(viewModel.getName());
        nameField.addTextChangedListener(new OnTextChangedWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setName(s.toString());
            }
        });

        EditText emailField = findViewById(R.id.add_email_field);
        emailField.setText(viewModel.getEmail());
        emailField.addTextChangedListener(new OnTextChangedWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setEmail(s.toString());
            }
        });

        EditText phoneField = findViewById(R.id.add_phone_field);
        phoneField.setText(viewModel.getPhone());
        phoneField.addTextChangedListener(new OnTextChangedWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setPhone(s.toString());
            }
        });

        final TextInputLayout dobLayout = findViewById(R.id.add_dob_layout);
        final EditText dobField = findViewById(R.id.add_dob_field);
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

        View btnAdd = findViewById(R.id.add_layout_btn);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: add to database
                finish();
            }
        });
    }
}
