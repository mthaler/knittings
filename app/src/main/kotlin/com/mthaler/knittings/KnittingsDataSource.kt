package com.mthaler.knittings

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import java.io.File
import java.util.ArrayList
import java.util.Date

class KnittingsDataSource private constructor(context: Context) {

    private val context: Context
    private val dbHelper: KnittingDatabaseHelper

    /**
     * Returns all knittings from the database
     *
     * @return all knittings from database
     */
    val allKnittings: ArrayList<Knitting>
        get() = dbHelper.writableDatabase.use { database ->
            val knittings = ArrayList<Knitting>()

            val cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS, KnittingDatabaseHelper.KnittingTable.Columns, null, null, null, null, null)

            cursor.moveToFirst()
            var knitting: Knitting

            while (!cursor.isAfterLast) {
                knitting = cursorToKnitting(cursor)
                knittings.add(knitting)
                Log.d(LOG_TAG, "Read knitting " + knitting.id + ", default photo: " + knitting.defaultPhoto)
                cursor.moveToNext()
            }

            cursor.close()

            return knittings
        }

    init {
        this.context = context.applicationContext
        dbHelper = KnittingDatabaseHelper(context)
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
    fun createKnitting(title: String, description: String, started: Date, finished: Date?, needleDiameter: Double, size: Double, rating: Double): Knitting {
        Log.d(LOG_TAG, "Creating knitting, title: " + title + ", description: " + description + ", started: " + started + ", finished: " +
                finished + ", needle diameter: " + needleDiameter + ", size: " + size + ", rating: " + rating)
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.TITLE, title)
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.DESCRIPTION, description)
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.STARTED, started.time)
            if (finished != null) {
                values.put(KnittingDatabaseHelper.KnittingTable.Cols.FINISHED, finished.time)
            }
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.NEEDLE_DIAMETER, needleDiameter)
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.SIZE, size)
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.RATING, rating)

            val id = database.insert(KnittingDatabaseHelper.KnittingTable.KNITTINGS, null, values)

            val cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    KnittingDatabaseHelper.KnittingTable.Columns, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val knittings = cursorToKnitting(cursor)
            cursor.close()

            return knittings
        }
    }

    /**
     * Updates a knitting in the database
     *
     * @param knitting knitting that should be updated
     * @return updated knitting
     */
    fun updateKnitting(knitting: Knitting): Knitting {
        Log.d(LOG_TAG, "Updating knitting " + knitting + ", default photo: " + knitting.defaultPhoto)
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.TITLE, knitting.title)
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.DESCRIPTION, knitting.description)
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.STARTED, knitting.started!!.time)
            if (knitting.finished != null) {
                values.put(KnittingDatabaseHelper.KnittingTable.Cols.FINISHED, knitting.finished!!.time)
            }
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.NEEDLE_DIAMETER, knitting.needleDiameter)
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.SIZE, knitting.size)
            if (knitting.defaultPhoto != null) {
                Log.d(LOG_TAG, "Default photo: " + knitting.defaultPhoto)
                values.put(KnittingDatabaseHelper.KnittingTable.Cols.DEFAULT_PHOTO_ID, knitting.defaultPhoto!!.id)
            } else {
                values.putNull(KnittingDatabaseHelper.KnittingTable.Cols.DEFAULT_PHOTO_ID)
            }
            values.put(KnittingDatabaseHelper.KnittingTable.Cols.RATING, knitting.rating)

            database.update(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    values,
                    KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + knitting.id, null)

            val cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    KnittingDatabaseHelper.KnittingTable.Columns, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + knitting.id, null, null, null, null)

            cursor.moveToFirst()
            val result = cursorToKnitting(cursor)
            cursor.close()

            return result
        }
    }

    /**
     * Deletes the given knitting from the database
     *
     * @param knitting knitting that should be deleted
     */
    fun deleteKnitting(knitting: Knitting) {
        val id = knitting.id

        dbHelper.writableDatabase.use { database ->
            database.delete(KnittingDatabaseHelper.KnittingTable.KNITTINGS, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + id, null)
            Log.d(LOG_TAG, "Deleted knitting " + id + ": " + knitting.toString())
        }
    }

    /**
     * Gets the knitting with the given id from the database
     *
     * @param id id of the knitting that should be read from database
     * @return knitting for the given id
     */
    fun getKnitting(id: Long): Knitting {
        Log.d(LOG_TAG, "Getting knitting for id " + id)
        dbHelper.writableDatabase.use { database ->
            val cursor = database.query(KnittingDatabaseHelper.KnittingTable.KNITTINGS,
                    KnittingDatabaseHelper.KnittingTable.Columns, KnittingDatabaseHelper.KnittingTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val knitting = cursorToKnitting(cursor)
            cursor.close()

            return knitting
        }
    }

    fun getPhotoFile(knitting: Knitting): File? {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
        return File(externalFilesDir, knitting.photoFilename)
    }

    /**
     * Gets the photo with the given id from the database
     *
     * @param id id of the photo that should be read from database
     * @return photo for the given id
     */
    fun getPhoto(id: Long): Photo {
        Log.d(LOG_TAG, "Getting photo for id " + id)
        dbHelper.writableDatabase.use { database ->
            val cursor = database.query(KnittingDatabaseHelper.PhotoTable.PHOTOS,
                    KnittingDatabaseHelper.PhotoTable.Columns, KnittingDatabaseHelper.PhotoTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val photo = cursorToPhoto(cursor)
            cursor.close()

            return photo
        }
    }

    /**
     * Get all photos for the given knitting
     *
     * @param knitting knitting to get photos for
     * @return list of photos for the given knitting
     */
    fun getAllPhotos(knitting: Knitting): ArrayList<Photo> {
        dbHelper.writableDatabase.use { database ->
            val photos = ArrayList<Photo>()

            val whereClause = KnittingDatabaseHelper.PhotoTable.Cols.KNITTING_ID + " = ?"
            val whereArgs = arrayOf(java.lang.Double.toString(knitting.id.toDouble()))
            val cursor = database.query(KnittingDatabaseHelper.PhotoTable.PHOTOS, KnittingDatabaseHelper.PhotoTable.Columns, whereClause, whereArgs, null, null, null)

            cursor.moveToFirst()
            var photo: Photo

            while (!cursor.isAfterLast) {
                photo = cursorToPhoto(cursor)
                photos.add(photo)
                Log.d(LOG_TAG, "Read photo " + photo)
                cursor.moveToNext()
            }

            cursor.close()

            return photos
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
    fun createPhoto(filename: File, knittingID: Long, preview: Bitmap, description: String): Photo {
        Log.d(LOG_TAG, "Creating photo for $filename, knitting id: $knittingID, preview: $preview, description: $description")
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(KnittingDatabaseHelper.PhotoTable.Cols.FILENAME, filename.absolutePath)
            values.put(KnittingDatabaseHelper.PhotoTable.Cols.KNITTING_ID, knittingID)
            values.put(KnittingDatabaseHelper.PhotoTable.Cols.DESCRIPTION, description)
            val previewBytes = Photo.getBytes(preview)
            if (previewBytes != null) {
                values.put(KnittingDatabaseHelper.PhotoTable.Cols.PREVIEW, previewBytes)
            } else {
                values.putNull(KnittingDatabaseHelper.PhotoTable.Cols.PREVIEW)
            }

            val id = database.insert(KnittingDatabaseHelper.PhotoTable.PHOTOS, null, values)

            val cursor = database.query(KnittingDatabaseHelper.PhotoTable.PHOTOS,
                    KnittingDatabaseHelper.PhotoTable.Columns, KnittingDatabaseHelper.PhotoTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val photo = cursorToPhoto(cursor)
            cursor.close()

            return photo
        }
    }

    /**
     * Updates a photo in the database
     *
     * @param photo photo that should be updated
     * @return updated photo
     */
    fun updatePhoto(photo: Photo): Photo {
        Log.d(LOG_TAG, "Updating photo " + photo)
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(KnittingDatabaseHelper.PhotoTable.Cols.FILENAME, photo.filename.absolutePath)
            values.put(KnittingDatabaseHelper.PhotoTable.Cols.KNITTING_ID, photo.knittingID)
            values.put(KnittingDatabaseHelper.PhotoTable.Cols.DESCRIPTION, photo.description)
            val previewBytes = Photo.getBytes(photo.preview)
            if (previewBytes != null) {
                values.put(KnittingDatabaseHelper.PhotoTable.Cols.PREVIEW, previewBytes)
            } else {
                values.putNull(KnittingDatabaseHelper.PhotoTable.Cols.PREVIEW)
            }

            database.update(KnittingDatabaseHelper.PhotoTable.PHOTOS,
                    values,
                    KnittingDatabaseHelper.PhotoTable.Cols.ID + "=" + photo.id, null)

            val cursor = database.query(KnittingDatabaseHelper.PhotoTable.PHOTOS,
                    KnittingDatabaseHelper.PhotoTable.Columns, KnittingDatabaseHelper.PhotoTable.Cols.ID + "=" + photo.id, null, null, null, null)

            cursor.moveToFirst()
            val result = cursorToPhoto(cursor)
            cursor.close()

            return result
        }
    }

    /**
     * Deletes the given photo from the database
     *
     * @param photo photo that should be deleted
     */
    fun deletePhoto(photo: Photo) {
        val id = photo.id

        dbHelper.writableDatabase.use { database ->
            database.delete(KnittingDatabaseHelper.PhotoTable.PHOTOS, KnittingDatabaseHelper.PhotoTable.Cols.ID + "=" + id, null)
            Log.d(LOG_TAG, "Deleted photo " + id + ": " + photo.toString())
        }
    }

    /**
     * Delete all photos for the given knitting
     *
     * @param knitting knitting to delete photos for
     */
    fun deleteAllPhotos(knitting: Knitting) {
        val id = knitting.id

        dbHelper.writableDatabase.use { database ->
            val whereClause = KnittingDatabaseHelper.PhotoTable.Cols.KNITTING_ID + "= ?"
            val whereArgs = arrayOf(java.lang.Long.toString(id))
            database.delete(KnittingDatabaseHelper.PhotoTable.PHOTOS, whereClause, whereArgs)
            Log.d(LOG_TAG, "Removed knitting " + id + ": " + knitting.toString())
        }
    }

    private fun cursorToKnitting(cursor: Cursor): Knitting {
        val idIndex = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.ID)
        val idTitle = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.TITLE)
        val idDescription = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.DESCRIPTION)
        val idStarted = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.STARTED)
        val idFinished = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.FINISHED)
        val idNeedleDiameter = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.NEEDLE_DIAMETER)
        val idSize = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.SIZE)
        val idDefaultPhoto = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.DEFAULT_PHOTO_ID)
        val idRating = cursor.getColumnIndex(KnittingDatabaseHelper.KnittingTable.Cols.RATING)

        val id = cursor.getLong(idIndex)
        val title = cursor.getString(idTitle)
        val description = cursor.getString(idDescription)
        val started = Date(cursor.getLong(idStarted))
        val finished = if (cursor.isNull(idFinished)) null else Date(cursor.getLong(idFinished))
        val needleDiameter = cursor.getDouble(idNeedleDiameter)
        val size = cursor.getDouble(idSize)
        val rating = cursor.getDouble(idRating)

        var defaultPhoto: Photo? = null
        if (!cursor.isNull(idDefaultPhoto)) {
            val defaultPhotoID = cursor.getLong(idDefaultPhoto)
            defaultPhoto = getPhoto(defaultPhotoID)
        }
        return Knitting(id, title = title, description = description, started = started, finished = finished, needleDiameter = needleDiameter, size = size, rating = rating, defaultPhoto = defaultPhoto)
    }

    private fun cursorToPhoto(cursor: Cursor): Photo {
        val idIndex = cursor.getColumnIndex(KnittingDatabaseHelper.PhotoTable.Cols.ID)
        val idPreview = cursor.getColumnIndex(KnittingDatabaseHelper.PhotoTable.Cols.PREVIEW)
        val idFilename = cursor.getColumnIndex(KnittingDatabaseHelper.PhotoTable.Cols.FILENAME)
        val idKnittingIndex = cursor.getColumnIndex(KnittingDatabaseHelper.PhotoTable.Cols.KNITTING_ID)
        val idDescription = cursor.getColumnIndex(KnittingDatabaseHelper.PhotoTable.Cols.DESCRIPTION)

        val id = cursor.getLong(idIndex)
        val filename = cursor.getString(idFilename)
        val previewBytes = if (cursor.isNull(idPreview)) null else cursor.getBlob(idPreview)
        val knittingID = cursor.getLong(idKnittingIndex)
        val description = cursor.getString(idDescription)

        val photo = Photo(id, File(filename), knittingID, description)
        if (previewBytes != null) {
            val options = BitmapFactory.Options()
            val preview = BitmapFactory.decodeByteArray(previewBytes, 0, previewBytes.size, options)
            return photo.copy(preview = preview)
        } else {
            return photo
        }
    }

    companion object {

        private val LOG_TAG = KnittingsDataSource::class.java.simpleName

        private var sKnittingsDataSource: KnittingsDataSource? = null

        fun getInstance(c: Context): KnittingsDataSource {
            if (sKnittingsDataSource == null) {
                sKnittingsDataSource = KnittingsDataSource(c)
            }
            return sKnittingsDataSource!!
        }
    }
}

