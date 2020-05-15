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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonebook.R;
import com.example.phonebook.data.Contact;
import com.example.phonebook.viewmodels.PhonebookViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    public static final int SAVE_CONTACT_REQUEST = 1;

    private PhonebookViewModel viewModel;
    private MenuItem searchMenuItem;
    private boolean useDarkTheme;
    private MainAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeFromSharedPrefs();
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.appbar_main));
        viewModel = new ViewModelProvider(this).get(PhonebookViewModel.class);
        setRecyclerView(viewModel);
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
                        showDeleteSnackBar(state.getId());
                        viewModel.delete(state.getId());
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
                startActivityForResult(intent, SAVE_CONTACT_REQUEST);
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
        listAdapter.animateClose();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SAVE_CONTACT_REQUEST && resultCode == RESULT_OK) {
            assert data != null;
            Contact contact = (Contact) data.getSerializableExtra("contact");
            boolean added = contact.getId() == 0;

            viewModel.save(contact);
            if (added) {
                showAddSnackBar(contact);
            } else {
                showUpdateSnackBar(contact);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private void setRecyclerView(PhonebookViewModel viewModel) {
        RecyclerView recyclerView = findViewById(R.id.list_main);
        LinearLayoutManager recyclerManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, recyclerManager.getOrientation()));
        listAdapter = new MainAdapter(this);
        recyclerView.setAdapter(listAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                listAdapter.animateClose();
            }
        });

        viewModel.getContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                listAdapter.setContacts(contacts);
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

    private Snackbar newSnackBar(String message, View.OnClickListener listener) {
        return Snackbar.make(findViewById(R.id.layout_main), message, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, listener);
    }

    private void showDeleteSnackBar(final long id) {
        String name = viewModel.getName(id);
        newSnackBar(getString(R.string.item_deleted, name), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.undoDelete();
            }
        }).show();
    }

    private void showAddSnackBar(final Contact contact) {
        String name = contact.getName();
        newSnackBar(getString(R.string.item_added, name), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.undoSave();
            }
        }).show();
    }

    private void showUpdateSnackBar(final Contact contact) {
        String name = contact.getName();
        newSnackBar(getString(R.string.item_updated, name), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.undoSave();
            }
        }).show();
    }
}
