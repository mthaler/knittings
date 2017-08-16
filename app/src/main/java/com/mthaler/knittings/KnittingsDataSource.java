package com.mthaler.knittings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

public class KnittingsDataSource {

    private static final String LOG_TAG = KnittingsDataSource.class.getSimpleName();

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

    /**
     * Creates a new knitting and adds it to the database
     *
     * @param title title
     * @param description description
     * @param started started date
     * @param finished finished date
     * @param needleDiameter needle diameter
     * @param size size of knitting
     * @return new knitting
     */
    public Knitting createKnitting(String title, String description, Date started, Date finished, double needleDiameter, double size) {
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
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.SIZE, size);

            final long id = database.insert(KnittingDatabaseHelper.KnittingTable.KNITTINGS, null, values);

            final Cursor cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    KnittingDatabaseHelper.KnittingTable.Columns, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + id, null, null, null, null);

            cursor.moveToFirst();
            final Knitting knittings = cursorToKnitting(cursor);
            cursor.close();

            return knittings;
        } finally {
            database.close();
        }
    }

    /**
     * Updates a knitting in the database
     *
     * @param knitting knitting that should be updated
     * @return updated knitting
     */
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
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.SIZE, knitting.getSize());

            database.update(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    values,
                    KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + knitting.getId(),
                    null);

            final Cursor cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    KnittingDatabaseHelper.KnittingTable.Columns, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + knitting.getId(), null, null, null, null);

            cursor.moveToFirst();
            final Knitting result = cursorToKnitting(cursor);
            cursor.close();

            return result;
        } finally {
            database.close();
        }
    }

    /**
     * Deletes the given knitting from the database
     *
     * @param knitting knitting that should be deleted
     */
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

    /**
     * Returns all knittings from the database
     *
     * @return all knittings from database
     */
    public ArrayList<Knitting> getAllKnittings() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final ArrayList<Knitting> knittings = new ArrayList<>();

            final Cursor cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS, KnittingDatabaseHelper.KnittingTable.Columns, null, null, null, null, null);

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

    /**
     * Gets the knitting with the given id from the database
     *
     * @param id id of the knitting that should be read from database
     * @return knitting for the given id
     */
    public Knitting getKnitting(long id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final Cursor cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    KnittingDatabaseHelper.KnittingTable.Columns, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + id, null, null, null, null);

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

    /**
     * Get all photos for the given knitting
     *
     * @param knitting knitting to get photos for
     * @return list of photos for the given knitting
     */
    public ArrayList<Photo> getAllPhotos(Knitting knitting) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final ArrayList<Photo> photos = new ArrayList<>();

            final String whereClause = KnittingDatabaseHelper.PhotoTable.Cols.KNITTING_ID + " = ?";
            final String[] whereArgs = { Double.toString(knitting.getId()) };
            final Cursor cursor = database.query(KnittingDatabaseHelper.PhotoTable.PHOTOS, KnittingDatabaseHelper.PhotoTable.Columns, whereClause, whereArgs, null, null, null);

            cursor.moveToFirst();
            Photo photo;

            while(!cursor.isAfterLast()) {
                photo = cursorToPhoto(cursor);
                photos.add(photo);
                Log.d(LOG_TAG, "ID: " + photo.getId() + ", Inhalt: " + photo.toString());
                cursor.moveToNext();
            }

            cursor.close();

            return photos;
        } finally {
            database.close();
        }
    }

    /**
     * Creates a new photo and adds it to the database
     *
     * @param filename filename of the photo
     * @param knittingID id of the knitting this photo belongs to
     * @param preview preview of the photo. Might be null
     * @return new photo
     */
    public Photo createPhoto(File filename, long knittingID, Bitmap preview) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final ContentValues values = new ContentValues();
            values.put(KnittingDatabaseHelper.PhotoTable.Cols.FILENAME, filename.getPath());
            values.put(KnittingDatabaseHelper.PhotoTable.Cols.KNITTING_ID, knittingID);
            if (preview != null) {
                final int byteCount = preview.getByteCount();
                final ByteBuffer buffer = ByteBuffer.allocate(byteCount);
                preview.copyPixelsToBuffer(buffer);
                values.put(KnittingDatabaseHelper.PhotoTable.Cols.PREVIEW, buffer.array());
            }

            final long id = database.insert(KnittingDatabaseHelper.PhotoTable.PHOTOS, null, values);

            final Cursor cursor = database.query(KnittingDatabaseHelper.PhotoTable.PHOTOS,
                    KnittingDatabaseHelper.PhotoTable.Columns, KnittingDatabaseHelper.PhotoTable.Cols.ID + "=" + id, null, null, null, null);

            cursor.moveToFirst();
            final Photo photo = cursorToPhoto(cursor);
            cursor.close();

            return photo;
        } finally {
            database.close();
        }
    }

    /**
     * Delete all photos for the given knitting
     *
     * @param knitting knitting to delete photos for
     */
    public void deleteAllPhotos(Knitting knitting) {
        final long id = knitting.getId();

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            final String whereClause = KnittingDatabaseHelper.PhotoTable.Cols.KNITTING_ID + "= ?";
            final String[] whereArgs = new String[] { Long.toString(id) };
            database.delete(KnittingDatabaseHelper.PhotoTable.PHOTOS, whereClause, whereArgs);
            Log.d(LOG_TAG, "Removed knitting " + id + ": " + knitting.toString());
        } finally {
            database.close();
        }
    }

    private Knitting cursorToKnitting(Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.ID);
        final int idTitle = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.TITLE);
        final int idDescription = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.DESCRIPTION);
        final int idStarted = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.STARTED);
        final int idFinished = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.FINISHED);
        final int idNeedleDiameter = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.NEEDLE_DIAMETER);
        final int idSize = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.SIZE);

        final long id = cursor.getLong(idIndex);
        final String title = cursor.getString(idTitle);
        final String description = cursor.getString(idDescription);
        final Date started = new Date(cursor.getLong(idStarted));
        final Date finished = cursor.isNull(idFinished) ? null : new Date(cursor.getLong(idFinished));
        final double needleDiameter = cursor.getDouble(idNeedleDiameter);
        final double size = cursor.getDouble(idSize);

        final Knitting knitting = new Knitting(id);
        knitting.setTitle(title);
        knitting.setDescription(description);
        knitting.setStarted(started);
        knitting.setFinished(finished);
        knitting.setNeedleDiameter(needleDiameter);
        knitting.setSize(size);

        return knitting;
    }

    private Photo cursorToPhoto(Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(KnittingDatabaseHelper.PhotoTable.Cols.ID);
        final int idPreview = cursor.getColumnIndex(KnittingDatabaseHelper.PhotoTable.Cols.PREVIEW);
        final int idFilename = cursor.getColumnIndex(KnittingDatabaseHelper.PhotoTable.Cols.FILENAME);
        final int idKnittingIndex = cursor.getColumnIndex(KnittingDatabaseHelper.PhotoTable.Cols.KNITTING_ID);

        final long id = cursor.getLong(idIndex);
        final String filename = cursor.getString(idFilename);
        final byte[] previewBytes = cursor.isNull(idPreview) ? null : cursor.getBlob(idPreview);
        final long knittingID = cursor.getLong(idKnittingIndex);

        final Photo photo = new Photo(id, new File(filename), knittingID);
        if (previewBytes != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap preview  = BitmapFactory.decodeByteArray(previewBytes, 0, previewBytes.length, options);
            photo.setPreview(preview);
        }
        return photo;
    }
}

