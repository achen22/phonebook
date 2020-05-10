package com.example.phonebook.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonebook.R;
import com.example.phonebook.data.Contact;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private MenuItem searchMenuItem;
    private boolean useDarkTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeFromSharedPrefs();
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.appbar_main));
        setRecyclerView();
        handleIntent(getIntent());

        View deleteBtn = findViewById(R.id.fab_delete);
        deleteBtn.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                MainAdapter.ItemState state = (MainAdapter.ItemState) event.getLocalState();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // Scroll to top of NestedScrollView to show this button
                        ((NestedScrollView) v.getParent().getParent()).smoothScrollTo(0, 0);
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                    case DragEvent.ACTION_DRAG_EXITED:
                        state.setDeleting(event.getAction() == DragEvent.ACTION_DRAG_ENTERED);
                        break;
                    case DragEvent.ACTION_DROP:
                        state.setDeleting(false);
                        // TODO: delete from database
                        Toast.makeText(v.getContext(), Long.toString(state.getId()), Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        View searchBtn = findViewById(R.id.fab_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchMenuItem.expandActionView();
            }
        });

        View addBtn = findViewById(R.id.fab_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        menu.findItem(R.id.action_dark_mode).setChecked(useDarkTheme);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();
        setSearchViewStyle(searchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        // this shows/hides the software keyboard when the search menu is expanded/collapsed
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.onActionViewExpanded();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchView.onActionViewCollapsed();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_dark_mode:
                searchMenuItem.collapseActionView();
                item.setChecked(!useDarkTheme);
                SharedPreferences preferences = getSharedPreferences(
                        getApplicationContext().getPackageName(), MODE_PRIVATE);
                preferences.edit()
                        .putBoolean(WelcomeActivity.DARK_THEME_KEY, !useDarkTheme)
                        .apply();
                recreate();
                break;

            case R.id.action_search:
                break;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        SharedPreferences preferences = getSharedPreferences(
                getApplicationContext().getPackageName(), MODE_PRIVATE);
        if (preferences.getBoolean(WelcomeActivity.DARK_THEME_KEY, false) != useDarkTheme) {
            recreate();
        }
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void setThemeFromSharedPrefs() {
        SharedPreferences preferences = getSharedPreferences(
                getApplicationContext().getPackageName(), MODE_PRIVATE);
        if (preferences.contains(WelcomeActivity.DARK_THEME_KEY)) {
            applyTheme(preferences);
        } else {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        }
    }

    private void applyTheme(@NonNull SharedPreferences preferences) {
        useDarkTheme = preferences.getBoolean(WelcomeActivity.DARK_THEME_KEY, false);
        setTheme(useDarkTheme ? R.style.DarkTheme : R.style.AppTheme);
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.list_main);
        LinearLayoutManager recyclerManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, recyclerManager.getOrientation()));
        final MainAdapter adapter = new MainAdapter(getSampleData());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                adapter.animateClose();
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query == null || query.isEmpty()) {
                searchMenuItem.expandActionView();
            } else {
                // TODO: use the query to filter recyclerView items
            }
        }
    }

    private void setSearchViewStyle(SearchView searchView) {
        // set colors
        int resId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText text = searchView.findViewById(resId);
        TypedArray attrs = getTheme().obtainStyledAttributes(new int[] {
                R.attr.colorOnPrimary, R.attr.hintTextColor
        });

        int color = attrs.getColor(0, getResources().getColor(
                R.color.design_default_color_on_primary));
        text.setTextColor(color);
        int alpha = (int) (0xFF * (useDarkTheme ? 0.38 : 0.50)); // @dimen/hint_alpha_material_*
        //int alpha = (int) (0xFF * (useDarkTheme ? 0.54 : 0.70)); // @dimen/hint_pressed_alpha_material_*
        text.setHintTextColor(alpha * 0x1000000 + color % 0x1000000);
        attrs.recycle();

        // remove search icon
        resId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        View searchIcon = searchView.findViewById(resId);
        ((ViewGroup) searchIcon.getParent()).removeView(searchIcon);

        // remove underline
        resId = getResources().getIdentifier("android:id/search_plate", null, null);
        searchView.findViewById(resId).setBackground(null);
    }

    private List<Contact> getSampleData() {
            // TODO: load from database
            ArrayList<Contact> contacts = new ArrayList<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String[][] data = {
                    {"Amy Norman", "daniel40@hotmail.com", "0492323972", "1958-05-21"},
                    {"Michelle Clayton", "kimberlyjackson@gmail.com", "0402014907", "1999-11-09"},
                    {"Michelle Rivera", "tamarabrooks@hotmail.com", "0413600973", "2009-01-02"},
                    {"Rebecca Smith", "jessicafrederick@yahoo.com", "0456911617", "1935-12-09"},
                    {"Douglas Jones", "dmccoy@hotmail.com", "0453064476", "1959-09-21"},
                    {"Anne Mcintosh", "mcfarlandandrew@hotmail.com", "0406489208", "1974-04-14"},
                    {"Erika Walker", "hmartin@gmail.com", "0443764605", "1986-07-16"},
                    {"Morgan Wood", "patricia59@gmail.com", "0439689172", "1958-11-10"},
                    {"John Chandler", "lbenson@gmail.com", "0426807804", "1982-01-06"},
                    {"Travis Russell", "ashleymiller@yahoo.com", "0408744311", "2019-05-26"},
                    {"Kimberly Porter", "harrisongeoffrey@gmail.com", "0422937083", "1970-06-29"},
                    {"Brandi Glass", "brianfrank@hotmail.com", "0475480073", "1929-06-25"},
                    {"Mikayla Weber", "yfarley@hotmail.com", "0405267001", "1914-09-26"},
                    {"Amy Collins", "william26@yahoo.com", "0437692474", "1947-02-20"},
                    {"Robin Cain", "melindasteele@gmail.com", "0498442314", "1930-05-09"},
                    {"Krista Holmes", "connercheryl@gmail.com", "0492958716", "1949-02-07"},
                    {"Barbara Gonzalez", "bbennett@gmail.com", "0408132618", "1990-12-16"},
                    {"Jessica Tucker", "karenpage@hotmail.com", "0401460751", "1912-05-16"},
                    {"Gary Weber", "tanyasmith@hotmail.com", "0439250899", "1998-04-11"},
                    {"Amy Murray", "kennethwebb@hotmail.com", "0448069040", "1994-11-02"},
                    {"Emily Frye", "davidgrant@gmail.com", "0464523178", "1907-09-05"},
                    {"Peter Kramer", "carrie14@hotmail.com", "0442862693", "1986-05-11"},
                    {"Jesse Martin", "jason91@yahoo.com", "0490512330", "1904-06-09"},
                    {"Angela Lewis", "dawnmartinez@yahoo.com", "0447421347", "2002-01-03"},
                    {"Laura Morris", "williamsongregory@gmail.com", "0468148637", "1948-09-09"},
                    {"Brenda Lee", "kristen20@yahoo.com", "0422512398", "1959-01-13"},
                    {"Dr. Tony Harris", "powens@yahoo.com", "0431113642", "2010-05-31"},
                    {"Yesenia Davis", "rhowell@gmail.com", "0489011859", "1946-11-09"},
                    {"Adam Watts", "jennifersnyder@hotmail.com", "0445580410", "1951-11-19"},
                    {"John Wilkins", "evan57@yahoo.com", "0442324376", "1949-06-06"}
            };

            for (int i = 0; i < data.length; i++) {
                try {
                    contacts.add(new Contact(i+1, data[i][0], data[i][1], data[i][2],
                            format.parse(data[i][3])));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return contacts;
    }
}
