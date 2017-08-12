package com.mthaler.knittings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;

public class KnittingsDataSource {

    private static final String LOG_TAG = KnittingsDataSource.class.getSimpleName();

    private String[] columns = {
            KnittingDatabaseHelper.COLUMN_ID,
            KnittingDatabaseHelper.COLUMN_TITLE,
            KnittingDatabaseHelper.COLUMN_DESCRIPTION,
            KnittingDatabaseHelper.COLUMN_STARTED,
            KnittingDatabaseHelper.COLUMN_FINISHED
    };


    private static KnittingsDataSource sKnittingsDataSource;

    private final KnittingDatabaseHelper dbHelper;

    private KnittingsDataSource(Context context) {

        dbHelper = new KnittingDatabaseHelper(context);
    }

    public static KnittingsDataSource getInstance(Context c) {
        if (sKnittingsDataSource == null) {
            sKnittingsDataSource = new KnittingsDataSource(c);
        }
        return sKnittingsDataSource;
    }

    public Knitting createKnitting(String title, String description, Date started, Date finished) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(KnittingDatabaseHelper.COLUMN_TITLE, title);
            values.put(KnittingDatabaseHelper.COLUMN_DESCRIPTION, description);
            values.put(KnittingDatabaseHelper.COLUMN_STARTED, started.getTime());
            if (finished != null) {
                values.put(KnittingDatabaseHelper.COLUMN_FINISHED, finished.getTime());
            }

            long insertId = database.insert(KnittingDatabaseHelper.TABLE_KNITTINGS, null, values);

            Cursor cursor = database.query(KnittingDatabaseHelper.TABLE_KNITTINGS,
                    columns, KnittingDatabaseHelper.COLUMN_ID + "=" + insertId, null, null, null, null);

            cursor.moveToFirst();
            Knitting knittings = cursorToKnitting(cursor);
            cursor.close();

            return knittings;
        } finally {
            database.close();
        }
    }

    public Knitting updateKnitting(Knitting knitting) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(KnittingDatabaseHelper.COLUMN_TITLE, knitting.getTitle());
            values.put(KnittingDatabaseHelper.COLUMN_DESCRIPTION, knitting.getDescription());
            values.put(KnittingDatabaseHelper.COLUMN_STARTED, knitting.getStarted().getTime());
            if (knitting.getFinished() != null) {
                values.put(KnittingDatabaseHelper.COLUMN_FINISHED, knitting.getFinished().getTime());
            }

            database.update(KnittingDatabaseHelper.TABLE_KNITTINGS,
                    values,
                    KnittingDatabaseHelper.COLUMN_ID + "=" + knitting.getId(),
                    null);

            Cursor cursor = database.query(KnittingDatabaseHelper.TABLE_KNITTINGS,
                    columns, KnittingDatabaseHelper.COLUMN_ID + "=" + knitting.getId(), null, null, null, null);

            cursor.moveToFirst();
            final Knitting result = cursorToKnitting(cursor);
            cursor.close();

            return result;
        } finally {
            database.close();
        }
    }


    public ArrayList<Knitting> getAllKnittings() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final ArrayList<Knitting> knittings = new ArrayList<>();

            final Cursor cursor = database.query(KnittingDatabaseHelper.TABLE_KNITTINGS, columns, null, null, null, null, null);

            cursor.moveToFirst();
            Knitting knitting;

            while(!cursor.isAfterLast()) {
                knitting = cursorToKnitting(cursor);
                knittings.add(knitting);
                Log.d(LOG_TAG, "ID: " + knitting.getId() + ", Inhalt: " + knitting.toString());
                cursor.moveToNext();
            }

            cursor.close();

            return knittings;
        } finally {
            database.close();
        }
    }

    public Knitting getKnitting(long id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final Cursor cursor = database.query(KnittingDatabaseHelper.TABLE_KNITTINGS,
                    columns, KnittingDatabaseHelper.COLUMN_ID + "=" + id, null, null, null, null);

            cursor.moveToFirst();
            final Knitting knitting = cursorToKnitting(cursor);
            cursor.close();

            return knitting;
        } finally {
            database.close();
        }
    }

    private Knitting cursorToKnitting(Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(KnittingDatabaseHelper.COLUMN_ID);
        final int idTitle = cursor.getColumnIndex(KnittingDatabaseHelper.COLUMN_TITLE);
        final int idDescription = cursor.getColumnIndex(KnittingDatabaseHelper.COLUMN_DESCRIPTION);
        final int idStarted = cursor.getColumnIndex(KnittingDatabaseHelper.COLUMN_STARTED);
        final int idFinished = cursor.getColumnIndex(KnittingDatabaseHelper.COLUMN_FINISHED);

        final long id = cursor.getLong(idIndex);
        final String title = cursor.getString(idTitle);
        final String description = cursor.getString(idDescription);
        final Date started = new Date(cursor.getLong(idStarted));
        final Date finished = cursor.isNull(idFinished) ? null : new Date(cursor.getLong(idFinished));

        final Knitting knitting = new Knitting(id);
        knitting.setTitle(title);
        knitting.setDescription(description);
        knitting.setStarted(started);
        knitting.setFinished(finished);

        return knitting;
    }
}

