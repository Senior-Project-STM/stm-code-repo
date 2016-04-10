package com.uiuc.stmbluetoothdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by chrx on 4/10/16.
 */
public class DetailedSavedScanFragment  extends Fragment {
    String name;
    String date;
    String filepath;

    public DetailedSavedScanFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        name = args.getString("name");
        date = args.getString("date");
        filepath = args.getString("filepath");
        setHasOptionsMenu(true);
    }
}
