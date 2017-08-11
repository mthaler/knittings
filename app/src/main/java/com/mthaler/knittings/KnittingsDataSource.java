package com.mthaler.knittings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class KnittingsDataSource {

    private static final String LOG_TAG = KnittingsDataSource.class.getSimpleName();

    private String[] columns = {
            KnittingDatabaseHelper.COLUMN_ID,
            KnittingDatabaseHelper.COLUMN_TITLE,
            KnittingDatabaseHelper.COLUMN_DESCRIPTION,
            KnittingDatabaseHelper.COLUMN_STARTED,
            KnittingDatabaseHelper.COLUMN_FINISHED
    };

    private SQLiteDatabase database;
    private final KnittingDatabaseHelper dbHelper;

    public KnittingsDataSource(Context context) {
        Log.d(LOG_TAG, "Datasource created database helper.");
        dbHelper = new KnittingDatabaseHelper(context);
    }

    public void open() {
        Log.d(LOG_TAG, "Requesting reference to database.");
        database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Got database reference. Path to database: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Closed database using database helper.");
    }
}

