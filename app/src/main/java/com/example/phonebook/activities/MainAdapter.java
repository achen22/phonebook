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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonebook.R;
import com.example.phonebook.data.Contact;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    private List<Contact> contacts = new ArrayList<>();
    private final FragmentActivity owner;
    private View openItem = null;

    static class MainViewHolder extends RecyclerView.ViewHolder {
        private CoordinatorLayout layout;
        MainViewHolder(CoordinatorLayout itemView) {
            super(itemView);
            layout = itemView;
        }
    }

    public MainAdapter(FragmentActivity owner) {
        this.owner = owner;
    }

    public void setContacts(final List<Contact> contacts) {
        DiffCallback callback = new DiffCallback(contacts);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        this.contacts.clear();
        this.contacts.addAll(contacts);
        result.dispatchUpdatesTo(MainAdapter.this);
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = owner.getLayoutInflater();
        CoordinatorLayout layout = (CoordinatorLayout) inflater
                .inflate(R.layout.list_item_contact, parent, false);
        return new MainViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        final Contact contact = contacts.get(position);
        final GestureDetector gestureDetector;

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
        gestureDetector = new GestureDetector(owner, new OnGestureListener(item, contact.getId()));
        item.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
        });

        View btnEdit = holder.layout.findViewById(R.id.fab_edit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(owner, EditActivity.class);
                intent.putExtra("id", contact.getId());
                owner.startActivityForResult(intent, MainActivity.SAVE_CONTACT_REQUEST);
            }
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

    private class OnGestureListener extends GestureDetector.SimpleOnGestureListener {
        private final View view;
        private final long id;

        OnGestureListener(View view, long id) {
            this.view = view;
            this.id = id;
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
            ItemState state = new ItemState(id, view);
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
        private long id;
        private View view;
        private ItemShadow shadow;
        private int bgColor;
        private int deleteColor;

        ItemState(long id, View view) {
            this.id = id;
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

        public long getId() {
            return id;
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
        animate(openItem, false);
        openItem = null;
    }
}
