package com.uiuc.stmbluetoothdemo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;

/**
 * Created by chrx on 4/4/16.
 */
public class SavedListFragment extends Fragment {
    SavedListAdapter adapter;
    SQLiteDatabase db;
    SQLiteDatabase writeDB;
    ScanResultDbHelper dbHelper;
    View myView;
    RecyclerView myRecyclerView;


    private MultiSelector mSelector = new MultiSelector();      //Allows you to select multiple entries, and delete them
    private ModalMultiSelectorCallback deleteMode = new ModalMultiSelectorCallback(mSelector) {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            getActivity().getMenuInflater().inflate(R.menu.delete_menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId()==  R.id.menu_item_delete_scan){
                // Need to finish the action mode before doing the following,
                // not after. No idea why, but it crashes.
                actionMode.finish();
                Cursor cursor = adapter.getCursor();
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    if (mSelector.isSelected(i, 0)) {
                        adapter.getItem(i);
                        Long timeDb = Long.parseLong(cursor.getString(1));
                        Log.v("Time", Long.toString(timeDb));
                        deleteScan(timeDb);
                        cursor = getCursor(); //Rerun the database query so that any deleted items are removed
                        adapter.notifyItemRemoved(i);
                    }
                }
                mSelector.clearSelections();
                return true;
            }
            return false;
        }
    };

    public void deleteScan(Long timestamp) {
        String selection = ScanResultContract.FeedEntry.TIME + "=?";
        String[] args = {Long.toString(timestamp)};
        writeDB.delete(ScanResultContract.FeedEntry.TABLE_NAME, selection, args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_saved, container, false);
        Cursor cursor = getCursor();
        adapter = new SavedListAdapter(getActivity(), cursor, mSelector, deleteMode);
        adapter.setOnItemClickListener(new SavedListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String name, String time, String filePath, String extraNotes) {
                ((MainActivity) getActivity()).openDetailedSavedScanFragment(name, time, filePath, extraNotes);
            }
        });

        myRecyclerView = (RecyclerView) myView.findViewById(R.id.my_recylcer_view);
        final RecyclerView.LayoutManager myLayoutManager = new LinearLayoutManager(this.getActivity());

        //Use a linear layout manager
        myRecyclerView.setLayoutManager(myLayoutManager);
        myRecyclerView.setAdapter(adapter);
        return myView;
    }

    /**
     * Creates a cursor which accesses the saved scans from the database
     * @return
     */
    public Cursor getCursor() {
        dbHelper = new ScanResultDbHelper(getActivity());
        db = dbHelper.getReadableDatabase();
        writeDB = dbHelper.getWritableDatabase();

        String[] projection = {
                ScanResultContract.FeedEntry.SCAN_NAME,
                ScanResultContract.FeedEntry.TIME,
                ScanResultContract.FeedEntry.FILE_PATH,
                ScanResultContract.FeedEntry.EXTRA_NOTES
        };

        String sortOrder = ScanResultContract.FeedEntry.TIME + " desc";

        Cursor cursor = db.query(ScanResultContract.FeedEntry.TABLE_NAME, //Table Name
                projection,  //The columns
                null,       //The columns for the WHERE clause
                null,       //The values for the WHERE clause
                null,       //Any row grouping option
                null,       //Any row filter options
                sortOrder); //The sort order

        return cursor;
    }


}
