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

    private static final String DB_NAME = "knittings.db";
    private static final int DB_VERSION = 1;

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
            public static final String SIZE = "size";
            public static final String DEFAULT_PHOTO_ID = "default_photo_id";
            public static final String RATING = "rating";
        }

        public static final String[] Columns = {
            Cols.ID,
            Cols.TITLE,
            Cols.DESCRIPTION,
            Cols.STARTED,
            Cols.FINISHED,
            Cols.NEEDLE_DIAMETER,
            Cols.SIZE,
            Cols.DEFAULT_PHOTO_ID,
            Cols.RATING
        };

        public static final String SQL_CREATE =
                "CREATE TABLE " + KNITTINGS +
                        "(" + Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        Cols.TITLE + " TEXT NOT NULL, " +
                        Cols.DESCRIPTION + " TEXT NOT NULL, " +
                        Cols.STARTED + " INTEGER NOT NULL DEFAULT 0, " +
                        Cols.FINISHED + " INTEGER, " +
                        Cols.NEEDLE_DIAMETER + " REAL NOT NULL DEFAULT 0.0, " +
                        Cols.SIZE + " REAL NOT NULL DEFAULT 0.0, " +
                        Cols.DEFAULT_PHOTO_ID + " INTEGER, " +
                        Cols.RATING + " REAL NOT NULL DEFAULT 0.0, " +
                        "FOREIGN KEY(" + Cols.DEFAULT_PHOTO_ID + ") REFERENCES " + PhotoTable.PHOTOS + "(" + PhotoTable.Cols.ID + "));";

        public static final String SQL_DROP = "DROP TABLE IF EXISTS " + KNITTINGS;
    }

    public static final class PhotoTable {
        public static final String PHOTOS = "photos";

        public static final class Cols {
            public static final String ID = "_id";
            public static final String FILENAME = "filename";
            public static final String PREVIEW = "preview";
            public static final String DESCRIPTION = "description";
            public static final String KNITTING_ID = "knitting_id";
        }

        public static final String[] Columns = {
                Cols.ID,
                Cols.FILENAME,
                Cols.PREVIEW,
                Cols.DESCRIPTION,
                Cols.KNITTING_ID
        };

        public static final String SQL_CREATE =
                "CREATE TABLE " + PHOTOS +
                        "(" + Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        Cols.FILENAME + " TEXT NOT NULL, " +
                        Cols.PREVIEW + " BLOB, " +
                        Cols.DESCRIPTION + " TEXT NOT NULL, " +
                        Cols.KNITTING_ID + " INTEGER NOT NULL, " +
                        "FOREIGN KEY(" + Cols.KNITTING_ID + ") REFERENCES " + KnittingTable.KNITTINGS + "(" + KnittingTable.Cols.ID + "));";

        public static final String SQL_DROP = "DROP TABLE IF EXISTS " + PHOTOS;
    }

    public KnittingDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "KnittingDatabaseHelper created database: " + getDatabaseName());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(KnittingTable.SQL_CREATE);
            Log.d(LOG_TAG, "Knitting table created with: " + KnittingTable.SQL_CREATE);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Could not create knitting table with: " + KnittingTable.SQL_CREATE + ": " + ex.getMessage());
        }
        try {
            db.execSQL(PhotoTable.SQL_CREATE);
            Log.d(LOG_TAG, "Photo table created with: " + PhotoTable.SQL_CREATE);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Could not create photo table with: " + PhotoTable.SQL_CREATE + ": " + ex.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "Knitting table with version number " + oldVersion + " will be dropped.");
        db.execSQL(KnittingTable.SQL_DROP);
        Log.d(LOG_TAG, "Photo table with version number " + oldVersion + " will be dropped.");
        db.execSQL(PhotoTable.SQL_DROP);
        onCreate(db);
    }
}
