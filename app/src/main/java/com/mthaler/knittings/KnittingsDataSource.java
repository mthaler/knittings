package com.mthaler.knittings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class KnittingsDataSource {

    private static final String LOG_TAG = KnittingsDataSource.class.getSimpleName();

    private String[] columns = {
            KnittingDatabaseHelper.KnittingTable.Cols.ID,
            KnittingDatabaseHelper.KnittingTable.Cols.TITLE,
            KnittingDatabaseHelper.KnittingTable.Cols.DESCRIPTION,
            KnittingDatabaseHelper.KnittingTable.Cols.STARTED,
            KnittingDatabaseHelper.KnittingTable.Cols.FINISHED,
            KnittingDatabaseHelper.KnittingTable.Cols.NEEDLE_DIAMETER
    };


    private static KnittingsDataSource sKnittingsDataSource;

    private final Context context;
    private final KnittingDatabaseHelper dbHelper;

    private KnittingsDataSource(Context context) {
        this.context = context;
        dbHelper = new KnittingDatabaseHelper(context);
    }

    public static KnittingsDataSource getInstance(Context c) {
        if (sKnittingsDataSource == null) {
            sKnittingsDataSource = new KnittingsDataSource(c);
        }
        return sKnittingsDataSource;
    }

    public Knitting createKnitting(String title, String description, Date started, Date finished, double needleDiameter) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final ContentValues values = new ContentValues();
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.TITLE, title);
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.DESCRIPTION, description);
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.STARTED, started.getTime());
            if (finished != null) {
                values.put(KnittingDatabaseHelper.KnittingTable.Cols.FINISHED, finished.getTime());
            }
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.NEEDLE_DIAMETER, needleDiameter);

            final long id = database.insert(KnittingDatabaseHelper.KnittingTable.KNITTINGS, null, values);

            final Cursor cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    columns, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + id, null, null, null, null);

            cursor.moveToFirst();
            final Knitting knittings = cursorToKnitting(cursor);
            cursor.close();

            return knittings;
        } finally {
            database.close();
        }
    }

    public Knitting updateKnitting(Knitting knitting) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final ContentValues values = new ContentValues();
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.TITLE, knitting.getTitle());
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.DESCRIPTION, knitting.getDescription());
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.STARTED, knitting.getStarted().getTime());
            if (knitting.getFinished() != null) {
                values.put(KnittingDatabaseHelper.KnittingTable.Cols.FINISHED, knitting.getFinished().getTime());
            }
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.NEEDLE_DIAMETER, knitting.getNeedleDiameter());

            database.update(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    values,
                    KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + knitting.getId(),
                    null);

            final Cursor cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    columns, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + knitting.getId(), null, null, null, null);

            cursor.moveToFirst();
            final Knitting result = cursorToKnitting(cursor);
            cursor.close();

            return result;
        } finally {
            database.close();
        }
    }

    public void deleteKnitting(Knitting knitting) {
        final long id = knitting.getId();

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.delete(KnittingDatabaseHelper.KnittingTable.KNITTINGS, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + id, null);
            Log.d(LOG_TAG, "Removed knitting " + id + ": " + knitting.toString());
        } finally {
            database.close();
        }
    }


    public ArrayList<Knitting> getAllKnittings() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final ArrayList<Knitting> knittings = new ArrayList<>();

            final Cursor cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS, columns, null, null, null, null, null);

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
            final Cursor cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    columns, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + id, null, null, null, null);

            cursor.moveToFirst();
            final Knitting knitting = cursorToKnitting(cursor);
            cursor.close();

            return knitting;
        } finally {
            database.close();
        }
    }

    public File getPhotoFile(Knitting knitting) {
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFilesDir == null) {
            return null;
        }
        return new File(externalFilesDir, knitting.getPhotoFilename());
    }


    private Knitting cursorToKnitting(Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.ID);
        final int idTitle = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.TITLE);
        final int idDescription = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.DESCRIPTION);
        final int idStarted = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.STARTED);
        final int idFinished = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.FINISHED);
        final int idNeedleDiameter = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.NEEDLE_DIAMETER);

        final long id = cursor.getLong(idIndex);
        final String title = cursor.getString(idTitle);
        final String description = cursor.getString(idDescription);
        final Date started = new Date(cursor.getLong(idStarted));
        final Date finished = cursor.isNull(idFinished) ? null : new Date(cursor.getLong(idFinished));
        final double needleDiameter = cursor.getDouble(idNeedleDiameter);

        final Knitting knitting = new Knitting(id);
        knitting.setTitle(title);
        knitting.setDescription(description);
        knitting.setStarted(started);
        knitting.setFinished(finished);
        knitting.setNeedleDiameter(needleDiameter);

        return knitting;
    }
}

