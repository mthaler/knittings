package com.mthaler.knittings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database helper class that defines our tables, columns and methods to create and drop tables
 */
public class KnittingDatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = KnittingDatabaseHelper.class.getSimpleName();

    public static final String DB_NAME = "knittings.db";
    public static final int DB_VERSION = 1;

    /**
     * Class that defines the knittings database table schema
     */
    public static final class KnittingTable {
        public static final String KNITTINGS = "knittings";

        public static final class Cols {
            public static final String ID = "_id";
            public static final String TITLE = "title";
            public static final String DESCRIPTION = "description";
            public static final String STARTED = "started";
            public static final String FINISHED = "finished";
            public static final String NEEDLE_DIAMETER = "needle_diameter";
        }
    }

    public static final String SQL_CREATE =
            "CREATE TABLE " + KnittingTable.KNITTINGS +
                    "(" + KnittingTable.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KnittingTable.Cols.TITLE + " TEXT NOT NULL, " +
                    KnittingTable.Cols.DESCRIPTION + " TEXT NOT NULL, " +
                    KnittingTable.Cols.STARTED + " INTEGER NOT NULL DEFAULT 0, " +
                    KnittingTable.Cols.FINISHED + " INTEGER, " +
                    KnittingTable.Cols.NEEDLE_DIAMETER + " REAL NOT NULL DEFAULT 0.0 " + ");";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + KnittingTable.KNITTINGS;

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
