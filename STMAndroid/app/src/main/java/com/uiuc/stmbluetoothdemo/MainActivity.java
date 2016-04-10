package com.uiuc.stmbluetoothdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    MainActivityFragment mainFrag;
    SavedListFragment savedFrag;
    DetailedSavedScanFragment detailFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
     * Calls the startScan method in the Fragment
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
     * Calls the save method in the Fragment
     * @param v
     */
    public void save(View v) {
        mainFrag.save();
    }

    public void openDetailedSavedScanFragment(String name, String date, String file_path) {
        detailFrag = new DetailedSavedScanFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("date", date);
        args.putString("file_path", file_path);
        detailFrag.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, detailFrag, "detailed").addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.action_saved) {
            savedFrag = new SavedListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, savedFrag, "saved").addToBackStack(null)
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }
}
