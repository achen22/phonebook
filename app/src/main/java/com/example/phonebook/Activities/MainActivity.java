package com.example.phonebook.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.phonebook.R;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startWelcomeActivityOnFirstRun();
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(TAG);
        }

        // TODO: link to appropriate floating action button
        View addBtn = findViewById(R.id.btn_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddActivity.class);
                startActivity(intent);
            }
        });

        // TODO: move this to RecyclerView.Adapter?
        View editBtn = findViewById(R.id.btn_edit);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditActivity.class);
                // TODO: use the appropriate id
                intent.putExtra("id", 5);
                startActivity(intent);
            }
        });

        // TODO: move this to RecyclerView.Adapter?
        View detailBtn = findViewById(R.id.btn_detail);
        detailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                // TODO: use the appropriate id
                intent.putExtra("id", 5);
                startActivity(intent);
            }
        });
    }

    private void startWelcomeActivityOnFirstRun() {
        // TODO: check if this is the first time the app has been opened
        Intent intent = new Intent(this, WelcomeActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                getApplicationContext(), R.anim.slide_in_left, R.anim.slide_out_right);
        startActivity(intent, options.toBundle());
    }
}
