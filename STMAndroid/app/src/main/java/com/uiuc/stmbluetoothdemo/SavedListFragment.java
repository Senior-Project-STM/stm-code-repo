package com.uiuc.stmbluetoothdemo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;

import java.io.File;

/**
 * Created by chrx on 4/4/16.
 */
public class SavedListFragment extends Fragment implements SearchView.OnQueryTextListener{
    SavedListAdapter adapter;
    SQLiteDatabase db;
    SQLiteDatabase writeDB;
    ScanResultDbHelper dbHelper;
    View myView;
    RecyclerView myRecyclerView;

    private MultiSelector mSelector = new MultiSelector();      //Allows you to select multiple entries, and delete them
    private ModalMultiSelectorCallback deleteMode = new ModalMultiSelectorCallback(mSelector) {     //The callback that is called when you long click and turn n multi select mode

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            getActivity().getMenuInflater().inflate(R.menu.delete_menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            if (menuItem.getItemId()==  R.id.menu_item_delete_scan){        //When you click on the delete button, this callback is called
                // Need to finish the action mode before doing the following
                // not after. No idea why, but it crashes.
                actionMode.finish();
                Cursor cursor = adapter.getCursor();
                for (int i = 0; i < adapter.getItemCount(); i++) {          //Loops through the cursor, and deletes all entries that have been pressed
                    if (mSelector.isSelected(i, 0)) {
                        adapter.getItem(i);
                        Long timeDb = Long.parseLong(cursor.getString(1));
                        Log.v("Time", Long.toString(timeDb));
                        deleteScan(timeDb);             //Deletes the scan from the database
                        deleteImage(cursor.getString(2));          //Deletes the image from the database
                        adapter.notifyItemRemoved(i);
                    }
                }
                adapter.changeCursor(getCursor("")); //Rerun the database query so that any deleted items are removed
                mSelector.clearSelections();          //Clear out the multiselect
                return true;
            }
            return false;
        }
    };

    /**
     * Deletes the scan with the given timestamp from the database
     * @param timestamp THe timestamp of the scan
     */
    public void deleteScan(Long timestamp) {
        String selection = ScanResultContract.FeedEntry.TIME + "=?";
        String[] args = {Long.toString(timestamp)};
        writeDB.delete(ScanResultContract.FeedEntry.TABLE_NAME, selection, args);
    }

    /**
     * Deletes and image from storage
     * @param filepath      The filepath of the image
     */
    public void deleteImage(String filepath) {
        File file = new File(filepath);
        file.delete();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new ScanResultDbHelper(getActivity());
        db = dbHelper.getReadableDatabase();
        writeDB = dbHelper.getWritableDatabase();
        setHasOptionsMenu(true);
//        setRetainInstance(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_saved_list, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);  //This sets up the searchview
        searchView.setOnQueryTextListener(this);    //This registers the onQueryTextListener to be the callback for the searchview

    }

    /**
     * The callback that is called when the search text changes
     * @param newText   The search query
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.changeCursor(getCursor(newText));
        adapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getActivity()).setTitle("Saved Scans");
        myView = inflater.inflate(R.layout.fragment_saved, container, false);
        Cursor cursor = getCursor("");
        adapter = new SavedListAdapter(getActivity(), cursor, mSelector, deleteMode);   //Set the Saved list adapter to be the adapter for the text view
        adapter.setOnItemClickListener(new SavedListAdapter.OnItemClickListener() {  //Opens up the DetailedSavedScanFragment on click
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
     * Creates a cursor which accesses the saved scans from the database.
     * Will also use the constraint if needed, which will search for items with names like the constraint
     * @return
     */
    public Cursor getCursor(String constraint) {
        Log.e("Constraint", constraint);
        String[] projection = {
                ScanResultContract.FeedEntry.SCAN_NAME,
                ScanResultContract.FeedEntry.TIME,
                ScanResultContract.FeedEntry.FILE_PATH,
                ScanResultContract.FeedEntry.EXTRA_NOTES
        };

        String[] selectionArgs = {"%" + constraint + "%"};

        String sortOrder = ScanResultContract.FeedEntry.TIME + " desc";

        Cursor cursor = db.query(ScanResultContract.FeedEntry.TABLE_NAME, //Table Name
                projection,  //The columns
                (constraint.equals("") ? null : ScanResultContract.FeedEntry.SCAN_NAME + " LIKE ?"),       //The columns for the WHERE clause
                (constraint.equals("") ? null : selectionArgs),       //The values for the WHERE clause
                null,       //Any row grouping option
                null,       //Any row filter options
                sortOrder); //The sort order

        return cursor;
    }


}
