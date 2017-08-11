package com.mthaler.knittings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class KnittingDatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = KnittingDatabaseHelper.class.getSimpleName();

    public static final String DB_NAME = "knittings.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_KNITTINGS = "knittings";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_STARTED = "started";
    public static final String COLUMN_FINISHED = "finished";

    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_KNITTINGS +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                    COLUMN_STARTED + " INTEGER NOT NULL DEFAULT 0, " +
                    COLUMN_FINISHED + " INTEGER);";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_KNITTINGS;

    public KnittingDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "KnittingDatabaseHelper created database: " + getDatabaseName());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(LOG_TAG, "Table created with: " + SQL_CREATE);
            db.execSQL(SQL_CREATE);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Could not create table: " + ex.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "Table with version number " + oldVersion + " will be dropped.");
        db.execSQL(SQL_DROP);
        onCreate(db);
    }
}
