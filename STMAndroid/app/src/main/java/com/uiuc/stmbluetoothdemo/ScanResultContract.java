package com.uiuc.stmbluetoothdemo;

import android.provider.BaseColumns;

/**
 * Created by chrx on 4/4/16.
 * This class represents the database schema for the scan result database
 */
public class ScanResultContract {

    //Use the empty constructor so that no one can instantiate it
    public ScanResultContract() {}

    //This defines the database schema for the Scan Results
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "scan_results";
        public static final String TIME = "time";
        public static final String SCAN_NAME = "scan_name";
        public static final String FILE_PATH = "file_path";
        public static final String EXTRA_NOTES = "notes";

    }
}
