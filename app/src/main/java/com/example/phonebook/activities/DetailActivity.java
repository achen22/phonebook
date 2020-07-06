package com.example.phonebook.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

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
        PhonebookRepository repository = PhonebookRepository.getInstance(getApplicationContext());
        final LiveData<Contact> liveData = repository.get(id);

        liveData.observe(this, new Observer<Contact>() {
            @Override
            public void onChanged(Contact contact) {
                liveData.removeObserver(this);

                ((TextView) findViewById(R.id.detail_name_text)).setText(contact.getName());

                TextView emailText = findViewById(R.id.detail_email_text);
                if (contact.getEmail() != null) {
                    emailText.setText(contact.getEmail());
                } else {
                    emailText.setText(R.string.empty_field);
                    emailText.setEnabled(false);
                }

                Button phoneText = findViewById(R.id.detail_phone_text);
                if (contact.getPhone() != null) {
                    phoneText.setText(contact.getPhone());
                    phoneText.setOnClickListener(v -> {
                        CharSequence phone = ((TextView) v).getText();
                        callPhone(phone);
                    });
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
            }
        });

        View btnBack = findViewById(R.id.detail_layout_btn);
        btnBack.setOnClickListener(view -> finish());
    }

    private void callPhone(CharSequence phone) {
        if (phone == null || phone.length() == 0) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);
    }
}
