package com.example.phonebook.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.phonebook.R;
import com.example.phonebook.data.Contact;
import com.example.phonebook.data.PhonebookRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DetailActivity extends BaseChildActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildView(R.layout.activity_detail);
        long id = getIntent().getLongExtra("id", 0);
        Contact contact = PhonebookRepository.getInstance().get(id);

        ((TextView) findViewById(R.id.detail_name_text)).setText(contact.getName());
        
        TextView emailText = findViewById(R.id.detail_email_text);
        if (contact.getEmail() != null) {
            emailText.setText(contact.getEmail());
        } else {
            emailText.setText(R.string.empty_field);
            emailText.setEnabled(false);
        }

        TextView phoneText = findViewById(R.id.detail_phone_text);
        if (contact.getPhone() != null) {
            phoneText.setText(contact.getPhone());
        } else {
            phoneText.setText(R.string.empty_field);
            phoneText.setEnabled(false);
        }
        
        TextView dobText = findViewById(R.id.detail_dob_text);
        if (contact.getDob() != null) {
            DateFormat format = SimpleDateFormat.getDateInstance();
            dobText.setText(format.format(contact.getDob()));
        } else {
            dobText.setText(R.string.empty_field);
            dobText.setEnabled(false);
        }

        View btnBack = findViewById(R.id.detail_layout_btn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
