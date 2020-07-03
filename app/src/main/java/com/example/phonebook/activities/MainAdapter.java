package com.example.phonebook.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonebook.R;
import com.example.phonebook.data.Contact;
import com.example.phonebook.data.ContactsHashTable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> implements SectionIndexer {
    private ContactsHashTable hashTable;
    private List<Contact> contacts = new ArrayList<>();
    private final MainActivity owner;
    private View openItem = null;

    static class MainViewHolder extends RecyclerView.ViewHolder {
        private CoordinatorLayout layout;
        MainViewHolder(CoordinatorLayout itemView) {
            super(itemView);
            layout = itemView;
        }
    }

    public MainAdapter(MainActivity owner) {
        this.owner = owner;
    }

    public void setContacts(final ContactsHashTable contacts) {
        hashTable = contacts;
        List<Contact> list = contacts.toList();
        DiffCallback callback = new DiffCallback(list);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        this.contacts.clear();
        this.contacts.addAll(list);
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = owner.getLayoutInflater();
        CoordinatorLayout layout = (CoordinatorLayout) inflater
                .inflate(R.layout.main_list_item, parent, false);
        return new MainViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        final Contact contact = contacts.get(position);
        final GestureDetector gestureDetector;
        owner.updateIndexCursor();

        ((TextView) holder.layout.findViewById(R.id.item_name_text)).setText(contact.getName());
        ((TextView) holder.layout.findViewById(R.id.item_email_text)).setText(contact.getEmail());
        ((TextView) holder.layout.findViewById(R.id.item_phone_text)).setText(contact.getPhone());

        TextView dobText = holder.layout.findViewById(R.id.item_dob_text);
        if (contact.getDob() != null) {
            DateFormat format = DateFormat.getDateInstance();
            dobText.setText(format.format(contact.getDob()));
        } else {
            dobText.setText(null);
        }

        View item = holder.layout.findViewById(R.id.main_list_item);
        gestureDetector = new GestureDetector(owner, new OnGestureListener(item, contact));
        item.setOnTouchListener((v, event) -> {
            boolean down = event.getActionMasked() == MotionEvent.ACTION_DOWN;
            boolean up = event.getActionMasked() == MotionEvent.ACTION_UP;
            if (up || down) {
                // onTouch should call View#performClick when a click is detected
                if (up) {
                    v.performClick();
                }

                // get material ripples to show properly
                v.setPressed(down);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    v.getForeground().setHotspot(event.getX(), event.getY());
                }
            }
            return gestureDetector.onTouchEvent(event);
        });

        View btnEdit = holder.layout.findViewById(R.id.fab_edit);
        btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(owner, EditActivity.class);
            intent.putExtra("id", contact.getId());
            owner.startActivityForResult(intent, MainActivity.SAVE_CONTACT_REQUEST);
        });
    }

    @Override
    public int getItemCount() {
        return contacts == null ? 0 : contacts.size();
    }

    public class DiffCallback extends DiffUtil.Callback {
        private List<Contact> oldList = contacts;
        private List<Contact> newList;

        public DiffCallback(List<Contact> newList) {
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }

    @Override
    public String[] getSections() {
        return hashTable.getSections();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return hashTable.getIndex()[sectionIndex];
    }

    @Override
    public int getSectionForPosition(int position) {
        return hashTable.getSectionForIndex(position);
    }

    public boolean isSectionEmpty(int sectionIndex) {
        return hashTable.isSectionEmpty(sectionIndex);
    }

    private class OnGestureListener extends GestureDetector.SimpleOnGestureListener {
        private final View view;
        private final long id;
        private final Contact contact;

        OnGestureListener(View view, Contact contact) {
            this.view = view;
            this.id = contact.getId();
            this.contact = contact;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Intent intent = new Intent(owner, DetailActivity.class);
            intent.putExtra("id", id);
            owner.startActivity(intent);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() > e2.getX()) {
                animateClose();
                animate(view, true); // right-to-left swipe
                owner.setFabVisible(false);
                openItem = view;

            } else if (view == openItem) {
                animateClose();
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            animateClose();
            openItem = null;
            ItemState state = new ItemState(contact, view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(null, state.getShadow(), state, 0);
            } else {
                view.startDrag(null, state.getShadow(), state, 0);
            }
        }

        public long getId() {
            return id;
        }
    }

    public static class ItemState {
        private Contact contact;
        private View view;
        private ItemShadow shadow;
        private int bgColor;
        private int deleteColor;

        ItemState(Contact contact, View view) {
            this.contact = contact;
            this.view = view;
            shadow = new ItemShadow(view.findViewById(R.id.item_name_text));
            bgColor = ((ColorDrawable) view.getBackground()).getColor();
            deleteColor = (bgColor & 0xFF999999) + 0x660000; // 40% red
        }

        private static class ItemShadow extends View.DragShadowBuilder {
            ItemShadow(View view) {
                super(view);
            }

            @Override
            public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
                super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint);
                // Draw the drag shadow to the left of the touch point
                outShadowTouchPoint.x = outShadowSize.x + (int)
                        (getView().getResources().getDimension(R.dimen.drag_shadow_offset));
            }
        }

        public Contact getContact() {
            return contact;
        }

        public View.DragShadowBuilder getShadow() {
            return shadow;
        }

        public void setDeleting(boolean deleting) {
            int startColor = ((ColorDrawable) view.getBackground()).getColor();
            int endColor = deleting ? deleteColor : bgColor;
            ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), startColor, endColor)
                    .start();
        }
    }

    private void animate(View view, boolean open) {
        if (view == null) {
            return;
        }

        float endPosition = open
                ? -view.getResources().getDimension(R.dimen.list_item_offset)
                : 0;
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX", endPosition);
        animX.setInterpolator(new FastOutSlowInInterpolator());
        animX.start();
    }

    public void animateClose() {
        if (openItem != null) {
            animate(openItem, false);
            openItem = null;
            owner.setFabVisible(true);
        }
    }
}
