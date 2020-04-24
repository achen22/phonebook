package com.example.phonebook.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.phonebook.R;

public class DetailActivity extends BaseChildActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildView(R.layout.activity_detail);

        // TODO: fill with data from database
        ((TextView) findViewById(R.id.detail_name_text)).setText(R.string.name_field);
        ((TextView) findViewById(R.id.detail_email_text)).setText(R.string.email_field);
        ((TextView) findViewById(R.id.detail_phone_text)).setText(R.string.phone_field);
        ((TextView) findViewById(R.id.detail_dob_text)).setText(R.string.dob_field);

        View btnBack = findViewById(R.id.detail_layout_btn);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
