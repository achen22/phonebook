package com.example.phonebook.activities;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonebook.R;
import com.example.phonebook.data.Contact;

import java.text.DateFormat;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    private List<Contact> contacts;

    public static class MainViewHolder extends RecyclerView.ViewHolder {
        private CoordinatorLayout layout;
        public MainViewHolder(CoordinatorLayout itemView) {
            super(itemView);
            layout = itemView;
        }
    }

    public MainAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CoordinatorLayout layout = (CoordinatorLayout) inflater
                .inflate(R.layout.item_contact, parent, false);
        return new MainViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        final Contact contact = contacts.get(position);

        TextView nameText = holder.layout.findViewById(R.id.item_name_text);
        nameText.setText(contact.getName());

        TextView emailText = holder.layout.findViewById(R.id.item_email_text);
        if (contact.getEmail() != null) {
            emailText.setText(contact.getEmail());
        } else {
            emailText.setText(R.string.empty_field);
        }

        TextView phoneText = holder.layout.findViewById(R.id.item_phone_text);
        if (contact.getPhone() != null) {
            phoneText.setText(contact.getPhone());
        } else {
            phoneText.setText(R.string.empty_field);
        }

        TextView dobText = holder.layout.findViewById(R.id.item_dob_text);
        if (contact.getDob() != null) {
            DateFormat format = DateFormat.getDateInstance();
            dobText.setText(format.format(contact.getDob()));
        } else {
            dobText.setText(R.string.empty_field);
        }

        View item = holder.layout.findViewById(R.id.main_list_item);
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                // TODO: use the appropriate id
                intent.putExtra("id", 5);
                view.getContext().startActivity(intent);
            }
        });

        View btnEdit = holder.layout.findViewById(R.id.fab_edit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditActivity.class);
                // TODO: use the appropriate id
                intent.putExtra("id", contact.getId());
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}
