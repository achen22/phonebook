package com.example.phonebook.Activities;

import android.os.Bundle;
import android.view.View;

import com.example.phonebook.R;

public class AddActivity extends BaseChildActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setChildView(R.layout.activity_add);

//        TextInputLayout dobField = findViewById(R.id.add_dob_field);
//        dobField.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(final View view, boolean hasFocus) {
//                if (hasFocus) {
//                    Calendar calendar = Calendar.getInstance();
//                    DatePickerDialog dialog = new DatePickerDialog(view.getContext(),
//                            new DatePickerDialog.OnDateSetListener() {
//                                @Override
//                                public void onDateSet(
//                                        DatePicker datePicker, int year, int month, int day) {
//                                    Calendar date = Calendar.getInstance();
//                                    date.set(year, month, day);
//                                    DateFormat format = SimpleDateFormat.getDateInstance();
//                                    ((EditText) view).setText(format.format(date.getTime()));
//                                }
//                            },
//                            calendar.get(Calendar.YEAR),
//                            calendar.get(Calendar.MONTH),
//                            calendar.get(Calendar.DAY_OF_MONTH));
//                    dialog.show();
//                }
//            }
//        });

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
