package com.uiuc.stmbluetoothdemo;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chrx on 4/4/16.
 */
public class SavedListFragment extends Fragment {
    SavedListAdapter adapter;
    SQLiteDatabase db;
    ScanResultDbHelper dbHelper;
    View myView;
    RecyclerView myRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_saved, container, false);
        Cursor cursor = getCursor();
        adapter = new SavedListAdapter(cursor);
        adapter.setOnItemClickListener(new SavedListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String name, String time, String file_path) {
                ((MainActivity) getActivity()).openDetailedSavedScanFragment(name, time, file_path);
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

        String[] projection = {
                ScanResultContract.FeedEntry.SCAN_NAME,
                "strftime('%m-%d-%Y', " + ScanResultContract.FeedEntry.TIME+ ", 'unixepoch')",
                ScanResultContract.FeedEntry.FILE_PATH
        };

        String sortOrder = "strftime('%m-%d-%Y', " + ScanResultContract.FeedEntry.TIME+ ", 'unixepoch') desc";

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
