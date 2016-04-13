package com.uiuc.stmbluetoothdemo;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * Created by chrx on 4/10/16.
 */
public class DetailedSavedScanFragment  extends Fragment {
    String name;
    String date = "Saved on: ";
    String filepath;
    String notes;
    View myView;
    ShareActionProvider mShareActionProvider;

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
        //setHasOptionsMenu(true);
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
                ((MainActivity) getActivity()).setTitle(nameView.getText().toString());
            }
        });
        nameView.setText(name);
        dateView.setText(date);
        extraNotes.setText(notes);
        ((MainActivity) getActivity()).setTitle(name);

        return myView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate menu resource file.
        inflater.inflate(R.menu.menu_detail_saved, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
//
//        // Fetch and store ShareActionProvider
//        mShareActionProvider = (ShareActionProvider) new ShareActionProvider(getActivity());
//        MenuItemCompat.setActionProvider(item, mShareActionProvider);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.menu_item_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);            //Sets up the share intent so that you can share the image
            shareIntent.setType("image/");
            Log.e("File path", filepath);
            Log.e("File path", filepath);
            Log.e("File path", filepath);
            Log.e("File path", filepath);
            Uri uri = Uri.fromFile(new File(filepath));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri.toString());
            startActivity(Intent.createChooser(shareIntent, "Share image using"));
        }

        return super.onOptionsItemSelected(item);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            Log.d("Share", "Not Null");
            mShareActionProvider.setShareIntent(shareIntent);
        }
        else{
            Log.d("Share", "Null");
        }
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
