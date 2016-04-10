package com.uiuc.stmbluetoothdemo;

import android.database.Cursor;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by chrx on 4/7/16.
 */
public class SavedListAdapter extends RecyclerView.Adapter<SavedListAdapter.ViewHolder> {
    private Cursor cursor;
    OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String name, String time, String file_path);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //Each data item is a CardView
        public CardView myCardView;

        public ViewHolder(CardView v) {
            super(v);
            v.setOnClickListener(this);
            myCardView = v;
        }

        @Override
        public void onClick(View v) {
            String text = ((TextView) (v.findViewById(R.id.name))).getText().toString();
            Snackbar.make(v, "Clicked on " + text, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            if (itemClickListener != null) {
                String nameDb = cursor.getString(0);
                String timeDb = cursor.getString(1);
                String pathDb = cursor.getString(2);
                itemClickListener.onItemClick(nameDb, timeDb, pathDb);
            }
        }
    }

    //Constructor. It accepts a database cursor which access all of the saved scans.
    public SavedListAdapter(Cursor cursor) {
        this.cursor = cursor;
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

        getItem(position);

        String nameDb = cursor.getString(0);
        String timeDb = cursor.getString(1);

        name.setText(nameDb);
        date.setText(timeDb);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
}



