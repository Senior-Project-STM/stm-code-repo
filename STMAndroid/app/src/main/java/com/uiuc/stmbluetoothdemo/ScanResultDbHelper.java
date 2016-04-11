package com.uiuc.stmbluetoothdemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chrx on 4/4/16.
 */
public class ScanResultDbHelper extends SQLiteOpenHelper {

    //This is the version. It must be incremented if the schema changes
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ScanResult.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ScanResultContract.FeedEntry.TABLE_NAME + "(" +
                    ScanResultContract.FeedEntry.TIME + " INTEGER PRIMARY KEY," +
                    ScanResultContract.FeedEntry.SCAN_NAME + TEXT_TYPE + COMMA_SEP +
                    ScanResultContract.FeedEntry.FILE_PATH + TEXT_TYPE + ");";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ScanResultContract.FeedEntry.TABLE_NAME;

    public ScanResultDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now, if the schema changes, the old schema is simply discarded
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
