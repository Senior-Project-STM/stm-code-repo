package com.uiuc.stmbluetoothdemo;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chrx on 4/10/16.
 */
public class DetailedSavedScanFragment  extends Fragment {
    String name;
    String date = "Saved on: ";
    String filepath;
    String notes;
    View myView;

    public DetailedSavedScanFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        name = args.getString("name");
        date += args.getString("date");
        filepath = args.getString("file_path");
        notes = args.getString("extra_notes");
        Log.v("Detailed View", filepath);
        setHasOptionsMenu(true);
//        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.detailed_saved_scan_fragment, container, false);

        ImageView imageView = (ImageView) myView.findViewById(R.id.imageView);
        final EditText nameView = (EditText) myView.findViewById(R.id.updatedTitle);
        TextView dateView = (TextView) myView.findViewById(R.id.time);
        final EditText extraNotes = (EditText) myView.findViewById(R.id.extra_notes);
        Button updateButton = (Button) myView.findViewById(R.id.updateButton);

        imageView.setImageBitmap(getBitmap());
        imageView.setOnClickListener(new View.OnClickListener() {           // A click listener for the image
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();         //Create an image Dialog to display the scan image as a large image
                ImageDialogFragment frag = ImageDialogFragment.newInstance(filepath);
                frag.show(fm, "image");

            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDb(nameView.getText().toString(), extraNotes.getText().toString());
            }
        });
        nameView.setText(name);
        dateView.setText(date);
        extraNotes.setText(notes);

        return myView;
    }

    /**
     * Opens up the requested image from the file directory, and returns it
     * @return
     */
    public Bitmap getBitmap() {
        BitmapFactory.Options option = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, option);
        return bitmap;
    }

    /**
     * Update the database with new values for the name and the extraNotes
     * @param name
     * @param extraNotes
     */
    public void updateDb(String name, String extraNotes) {
        ScanResultDbHelper dbHelper = new ScanResultDbHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(ScanResultContract.FeedEntry.SCAN_NAME, name);
        values.put(ScanResultContract.FeedEntry.EXTRA_NOTES, extraNotes);

        String selection = ScanResultContract.FeedEntry.FILE_PATH + " LIKE ? ";
        String[] selectionArgs = {filepath};

        db.update(ScanResultContract.FeedEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
