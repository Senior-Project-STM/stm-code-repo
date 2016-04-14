package com.uiuc.stmbluetoothdemo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    MainActivityFragment mainFrag;
    SavedListFragment savedFrag;
    DetailedSavedScanFragment detailFrag;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainFrag = new MainActivityFragment();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mainFrag, "main")
                    .commit();
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Calls the startBluetooth method in the Fragment
     * @param v
     */
    public void startBluetooth(View v) {
        mainFrag.startBluetooth();
    }

    /**
     * Calls the startScan method in the MainFragment
     * @param v
     */
    public void startScan(View v) {
        mainFrag.startScan();
    }


    /**
     * Calls the reset method in the Fragment
     * @param v
     */
    public void reset(View v) {
        mainFrag.reset();
    }

    /**
     * Calls the disconnect method in the MainFragment
     * @param v
     */
    public void disconnect(View v) {
        mainFrag.disconnect();
    }

    /**
     * Calls the save method in the MainFragment
     * @param v
     */
    public void save(View v) {
        mainFrag.save();
    }

    /**
     * Opens up a detailedScanFragment using the passed in parameters
     * @param name  The name
     * @param date The date
     * @param filePath The file path
     * @param extraNotes Any extra notes
     */
    public void openDetailedSavedScanFragment(String name, String date, String filePath, String extraNotes) {
        detailFrag = new DetailedSavedScanFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("date", date);
        args.putString("file_path", filePath);
        args.putString("extra_notes", extraNotes);
        detailFrag.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, detailFrag, "detailed").addToBackStack("detailed")
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_saved) {
            savedFrag = new SavedListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, savedFrag, "saved").addToBackStack("saved")
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Use this to set the title, to show whether we are connected to or disconnected to a microscope
     * @param title
     */
    public void setTitle(String title) {
        toolbar.setTitle(title);
    }

}
