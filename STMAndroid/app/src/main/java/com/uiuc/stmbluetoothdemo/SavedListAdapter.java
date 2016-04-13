package com.uiuc.stmbluetoothdemo;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by chrx on 4/7/16.
 */
public class SavedListAdapter extends RecyclerView.Adapter<SavedListAdapter.ViewHolder> {
    private Cursor cursor;
    OnItemClickListener itemClickListener;
    MultiSelector mSelector;
    ModalMultiSelectorCallback deleteMode;
    Activity act;

    public interface OnItemClickListener {
        void onItemClick(String name, String time, String filePath, String extraNotes);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {
        //Each data item is a CardView
        public CardView myCardView;
        public long time;

        public ViewHolder(CardView v) {
            super(v, mSelector);
            v.setOnClickListener(this);
            v.setLongClickable(true);
            v.setOnLongClickListener(this);
            myCardView = v;
        }

        @Override
        public void onClick(View v) {
            if (!mSelector.tapSelection(this)) {        //If it wasn't clicked with the multiselector open
                String text = ((TextView) (v.findViewById(R.id.name))).getText().toString();
                Snackbar.make(v, "Clicked on " + text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (itemClickListener != null) {
                    getItem(this.getPosition());
                    String nameDb = cursor.getString(0);
                    Long timeDb = Long.parseLong(cursor.getString(1));
                    String pathDb = cursor.getString(2);
                    String extraNotes = cursor.getString(3);
                    itemClickListener.onItemClick(nameDb, DateFormat.getDateTimeInstance().format(timeDb), pathDb, extraNotes);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            ((AppCompatActivity) act).startSupportActionMode(deleteMode);
            mSelector.setSelected(this, true);
            return true;
        }
    }

    //Constructor. It accepts a database cursor which access all of the saved scans.
    public SavedListAdapter(Activity act, Cursor cursor, MultiSelector mSelector, ModalMultiSelectorCallback deleteMode) {
        this.cursor = cursor;
        this.mSelector = mSelector;
        this.deleteMode = deleteMode;
        this.act = act;
    }

    /**
     * Allows outside class to access the cursor
     * @return
     */
    public Cursor getCursor() {
        return this.cursor;
    }

    //Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView v = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_list_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    /**
    * Accesses te db and moves the cursor to the given position
    * @param position The position that is being accessed
    * @return
     */
    public void getItem(final int position) {
        if (this.cursor != null && !this.cursor.isClosed()) {
            this.cursor.moveToPosition(position);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        View v = holder.myCardView;


        TextView name = (TextView) v.findViewById(R.id.name);
        TextView date = (TextView) v.findViewById(R.id.date);
        ImageView imageView = (ImageView) v.findViewById(R.id.image_thumbnail);

        getItem(position);

        String nameDb = cursor.getString(0);
        Long timeDb = Long.parseLong(cursor.getString(1));
        String filepath = cursor.getString(2);

        name.setText(nameDb);
        date.setText(DateFormat.getDateTimeInstance().format(timeDb));
        imageView.setImageBitmap(getBitmap(filepath));
    }

    /**
     * Open up the bitmap from storage
     * @return The bitmap
     */
    public Bitmap getBitmap(String filepath) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, option);
        return bitmap;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
}



