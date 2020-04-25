package com.example.phonebook.activities;

import android.os.Bundle;
import android.view.View;

import com.example.phonebook.R;

public class EditActivity extends BaseChildActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildView(R.layout.activity_edit);

        View btnEdit = findViewById(R.id.edit_layout_btn);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: update database
                finish();
            }
        });
    }
}
