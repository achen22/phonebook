package com.example.phonebook.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;

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
import com.example.phonebook.data.ContactsHashTable;
import com.example.phonebook.viewmodels.PhonebookViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    public static final int SAVE_CONTACT_REQUEST = 1;

    private PhonebookViewModel viewModel;
    private MenuItem searchMenuItem;
    private boolean useDarkTheme;
    private MainAdapter listAdapter;
    private int currentSection = 0;

    private int shortAnimTime;

    private View.OnTouchListener startDragOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                v.performClick();
                v = findViewById(R.id.index_cursor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    v.startDragAndDrop(null, new View.DragShadowBuilder(), null, 0);
                } else {
                    v.startDrag(null, new View.DragShadowBuilder(), null, 0);
                }
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeFromSharedPrefs();
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.appbar_main));
        viewModel = new ViewModelProvider(this).get(PhonebookViewModel.class);
        shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        setRecyclerView();
        handleIntent(getIntent());

        View deleteBtn = findViewById(R.id.fab_delete);
        deleteBtn.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                MainAdapter.ItemState state = (MainAdapter.ItemState) event.getLocalState();
                if (state == null) { // index drag
                    return false;
                }
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

        View sortBtn = findViewById(R.id.fab_sort_az);
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.setReverse(false);
            }
        });

        View reverseSortBtn = findViewById(R.id.fab_sort_za);
        reverseSortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.setReverse(true);
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
                viewModel.setSearchString("");
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null) {
                    newText = "";
                }
                viewModel.setSearchString(newText);
                return true;
            }
        });

        // restore previous search result
        String search = viewModel.getSearchString();
        if (!search.isEmpty()) {
            searchMenuItem.expandActionView();
            searchView.setQuery(search, false);
        }
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
            boolean added = contact.getId() == -1;

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

    private void setRecyclerView() {
        final ViewGroup indexLayout = findViewById(R.id.main_index);
        indexLayout.setOnTouchListener(startDragOnTouchListener);
        final TextView magnifier = findViewById(R.id.index_magnifier);
        indexLayout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        if (event.getLocalState() != null) {
                            return false;
                        }
                        magnifier.animate().cancel();
                        magnifier.setAlpha(1f);
                        magnifier.setVisibility(View.VISIBLE);
                        updateMagnifierPosition(currentSection);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        magnifier.animate().alpha(0f).setDuration(shortAnimTime)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        magnifier.setVisibility(View.GONE);
                                    }
                                });
                        break;
                }
                return true;
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.list_main);
        final LinearLayoutManager recyclerManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, recyclerManager.getOrientation()));
        listAdapter = new MainAdapter(this);
        recyclerView.setAdapter(listAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    listAdapter.animateClose();
                }
                updateIndex();
            }
        });

        viewModel.getContacts().observe(this, new Observer<ContactsHashTable>() {
            @Override
            public void onChanged(ContactsHashTable contacts) {
                final View listLayout = findViewById(R.id.list_main_layout);
                final View emptyLayout = findViewById(R.id.empty_state_layout);
                if (contacts.isEmpty()) {
                    setFabVisible(false);
                    if (listLayout.getVisibility() != View.GONE
                            || emptyLayout.getVisibility() != View.VISIBLE) {
                        // cross-fade to empty state layout
                        emptyLayout.setVisibility(View.VISIBLE);
                        emptyLayout.animate().alpha(1f).setDuration(shortAnimTime).setListener(null);
                        listLayout.animate().alpha(0f).setDuration(shortAnimTime)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        listLayout.setVisibility(View.GONE);
                                    }
                                });
                    }
                    if (searchMenuItem != null && searchMenuItem.isActionViewExpanded()) {
                        // empty result set
                        findViewById(R.id.empty_result_image).setVisibility(View.VISIBLE);
                        findViewById(R.id.empty_data_image).setVisibility(View.GONE);
                        ((TextView) findViewById(R.id.empty_state_text)).setText(R.string.empty_result);
                    } else {
                        // empty data set
                        if (searchMenuItem != null) {
                            searchMenuItem.setVisible(false);
                        }
                        findViewById(R.id.empty_result_image).setVisibility(View.GONE);
                        findViewById(R.id.empty_data_image).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.empty_state_text)).setText(R.string.empty_data);
                        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
                        fabAdd.show();
                    }
                } else {
                    setFabVisible(true);
                    if (searchMenuItem != null) {
                        searchMenuItem.setVisible(true);
                    }
                    if (emptyLayout.getVisibility() != View.GONE
                            || listLayout.getVisibility() != View.VISIBLE) {
                        listLayout.setVisibility(View.VISIBLE);
                        listLayout.animate().alpha(1f).setDuration(shortAnimTime).setListener(null);
                        // cross-fade to list view layout
                        emptyLayout.animate().alpha(0f).setDuration(shortAnimTime)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        emptyLayout.setVisibility(View.GONE);
                                    }
                                });
                    }
                    listAdapter.setContacts(contacts);
                    setIndexView(contacts.getSections());
                }
            }
        });
    }

    private void setIndexView(final String[] sections) {
        final ViewGroup indexLayout = findViewById(R.id.main_index);

        // Set number of child views
        if (sections.length != 0 && sections.length != indexLayout.getChildCount()) {
            int diff = sections.length - indexLayout.getChildCount();
            if (diff > 0) {
                for (int i = 0; i < diff; i++) {
                    View v = getLayoutInflater().inflate(R.layout.main_index_item, indexLayout, false);
                    v.setOnTouchListener(startDragOnTouchListener);
                    v.setOnDragListener(new View.OnDragListener() {
                        final int section = indexLayout.getChildCount();

                        @Override
                        public boolean onDrag(View v, DragEvent event) {
                            if (event.getAction() == DragEvent.ACTION_DRAG_STARTED && event.getLocalState() != null) {
                                return false;
                            }

                            if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED && !listAdapter.isSectionEmpty(section)) {
                                scrollTo(listAdapter.getPositionForSection(section));
                                updateMagnifierPosition(section);
                                updateIndex();
                            }
                            return true;
                        }

                        private void scrollTo(int pos) {
                            RecyclerView recyclerView = findViewById(R.id.list_main);
                            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                            assert layoutManager != null;
                            layoutManager.scrollToPositionWithOffset(pos, 0);
                        }
                    });
                    indexLayout.addView(v);
                }
            }
        }

        // Set text for child views
        for (int i = 0; i < sections.length; i++) {
            TextView view = (TextView) indexLayout.getChildAt(i);
            view.setText(sections[i]);
            view.setAlpha(listAdapter.isSectionEmpty(i) ? 0.4f : 1f);
        }
        updateIndex();
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query == null) {
                query = "";
            }
            viewModel.setSearchString(query);
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

    public void setFabVisible(boolean visible) {
        ViewGroup fabs = (ViewGroup) findViewById(R.id.fab_add).getParent();
        int count = fabs.getChildCount();
        if (visible) {
            for (int i = 0; i < count; i++) {
                ((FloatingActionButton) fabs.getChildAt(i)).show();
            }
        } else {
            for (int i = 0; i < count; i++) {
                ((FloatingActionButton) fabs.getChildAt(i)).hide();
            }
        }
    }

    public void updateIndex() {
        ViewGroup indexLayout = findViewById(R.id.main_index);
        if (indexLayout.getChildCount() == 0) {
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.list_main);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null) {
            return;
        }

        int pos = layoutManager.findFirstCompletelyVisibleItemPosition();
        if (pos == RecyclerView.NO_POSITION) {
            return;
        }

        int index = listAdapter.getSectionForPosition(pos);
        if (index == -1 || currentSection == index) {
            return;
        }

        currentSection = index;
        View indexView = indexLayout.getChildAt(index);
        float y = indexView.getY() + (indexView.getPivotY() + indexView.getBaseline()) / 2;
        View cursor = findViewById(R.id.index_cursor);
        cursor.animate().translationY(y - cursor.getPivotY()).setDuration(shortAnimTime);
    }

    public void updateMagnifierPosition(int index) {
        ViewGroup indexLayout = findViewById(R.id.main_index);
        TextView indexView = (TextView) indexLayout.getChildAt(index);
        float y = indexView.getY() + (indexView.getPivotY() + indexView.getBaseline()) / 2;
        TextView magnifier = findViewById(R.id.index_magnifier);
        magnifier.setY(Math.max(0, y - magnifier.getHeight()));
        magnifier.setText(indexView.getText());
    }

    private Snackbar newSnackBar(String message, View.OnClickListener listener) {
        return Snackbar.make(findViewById(R.id.layout_main), message, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, listener)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
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
