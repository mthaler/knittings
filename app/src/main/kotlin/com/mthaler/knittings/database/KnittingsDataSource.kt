package com.mthaler.knittings.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Environment
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.io.File
import java.util.ArrayList
import java.util.Date
import android.support.v4.app.Fragment
import com.mthaler.knittings.database.table.CategoryTable
import com.mthaler.knittings.database.table.KnittingTable
import com.mthaler.knittings.database.table.PhotoTable
import org.jetbrains.anko.error
import com.mthaler.knittings.database.table.CategoryTable.cursorToCategory
import com.mthaler.knittings.database.table.NeedleTable
import com.mthaler.knittings.database.table.PhotoTable.cursorToPhoto
import com.mthaler.knittings.database.table.NeedleTable.cursorToNeedle
import com.mthaler.knittings.model.*
import java.lang.Exception

class KnittingsDataSource private constructor(context: Context): AnkoLogger {

    private val context: Context = context.applicationContext
    private val dbHelper: KnittingDatabaseHelper

    /**
     * Returns all knittings from the database
     *
     * @return all knittings from database
     */
    val allKnittings: ArrayList<Knitting>
        @Synchronized
        get() = dbHelper.readableDatabase.use { database ->
            val knittings = ArrayList<Knitting>()

            val cursor = database.query(KnittingTable.KNITTINGS, KnittingTable.Columns, null, null, null, null, null)

            cursor.moveToFirst()
            var knitting: Knitting

            while (!cursor.isAfterLast) {
                knitting = cursorToKnitting(cursor)
                knittings.add(knitting)
                debug("Read knitting " + knitting.id + ", default photo: " + knitting.defaultPhoto)
                cursor.moveToNext()
            }

            cursor.close()

            return knittings
        }

    /**
     * Returns all knittings from the database
     *
     * @return all knittings from database
     */
    val allPhotos: ArrayList<Photo>
        @Synchronized
        get() = dbHelper.readableDatabase.use { database ->
            val photos = ArrayList<Photo>()

            val cursor = database.query(PhotoTable.PHOTOS, PhotoTable.Columns, null, null, null, null, null)

            cursor.moveToFirst()
            var photo: Photo

            while (!cursor.isAfterLast) {
                photo = cursorToPhoto(cursor)
                photos.add(photo)
                debug("Read photo " + photo.id + " filename: " + photo.filename)
                cursor.moveToNext()
            }

            cursor.close()

            return photos
        }

    /**
     * Returns all knittings from the database
     *
     * @return all knittings from database
     */
    val allCategories: ArrayList<Category>
        @Synchronized
        get() = dbHelper.readableDatabase.use { database ->
            val categories = ArrayList<Category>()

            val cursor = database.query(CategoryTable.CATEGORY, CategoryTable.Columns, null, null, null, null, null)

            cursor.moveToFirst()
            var category: Category

            while (!cursor.isAfterLast) {
                category = cursorToCategory(cursor)
                categories.add(category)
                debug("Read category $category")
                cursor.moveToNext()
            }

            cursor.close()

            return categories
        }

    /**
     * Returns all needles from the database
     *
     * @return all needles from database
     */
    val allNeedles: ArrayList<Needle>
        @Synchronized
        get() = dbHelper.readableDatabase.use { database ->
            val needles = ArrayList<Needle>()

            val cursor = database.query(NeedleTable.NEEDLES, NeedleTable.Columns, null, null, null, null, null)

            cursor.moveToFirst()
            var needle: Needle

            while (!cursor.isAfterLast) {
                needle = cursorToNeedle(context, cursor)
                needles.add(needle)
                debug("Read category $needle")
                cursor.moveToNext()
            }

            cursor.close()

            return needles
        }

    init {
        dbHelper = context.database
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
    @Synchronized
    fun createKnitting(title: String, description: String, started: Date, finished: Date?, needleDiameter: String, size: String, rating: Double, status: String): Knitting {
        debug("Creating knitting, title: " + title + ", description: " + description + ", started: " + started + ", finished: " +
                finished + ", needle diameter: " + needleDiameter + ", size: " + size + ", rating: " + rating)
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(KnittingTable.Cols.TITLE, title)
            values.put(KnittingTable.Cols.DESCRIPTION, description)
            values.put(KnittingTable.Cols.STARTED, started.time)
            if (finished != null) {
                values.put(KnittingTable.Cols.FINISHED, finished.time)
            }
            values.put(KnittingTable.Cols.NEEDLE_DIAMETER, needleDiameter)
            values.put(KnittingTable.Cols.SIZE, size)
            values.put(KnittingTable.Cols.RATING, rating)
            values.put(KnittingTable.Cols.DURATION, 0L)
            values.put(KnittingTable.Cols.STATUS, status)
            values.putNull(KnittingTable.Cols.CATEGORY_ID)

            val id = database.insert(KnittingTable.KNITTINGS, null, values)

            val cursor = database.query(KnittingTable.KNITTINGS,
                    KnittingTable.Columns, KnittingTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val knittings = cursorToKnitting(cursor)
            cursor.close()

            return knittings
        }
    }

    /**
     * Adds the given knitting to the database
     *
     * @param knitting knitting that should be added to the database
     * @param manualID: use knitting ID instead of auto-imcremented id
     */
    @Synchronized
    fun addKnitting(knitting: Knitting, manualID: Boolean = false) {
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            if (manualID) {
                values.put(KnittingTable.Cols.ID, knitting.id)
            }
            values.put(KnittingTable.Cols.TITLE, knitting.title)
            values.put(KnittingTable.Cols.DESCRIPTION, knitting.description)
            values.put(KnittingTable.Cols.STARTED, knitting.started.time)
            if (knitting.finished != null) {
                values.put(KnittingTable.Cols.FINISHED, knitting.finished.time)
            }
            values.put(KnittingTable.Cols.NEEDLE_DIAMETER, knitting.needleDiameter)
            values.put(KnittingTable.Cols.SIZE, knitting.size)
            if (knitting.defaultPhoto != null) {
                debug("Default photo: " + knitting.defaultPhoto)
                values.put(KnittingTable.Cols.DEFAULT_PHOTO_ID, knitting.defaultPhoto.id)
            } else {
                values.putNull(KnittingTable.Cols.DEFAULT_PHOTO_ID)
            }
            values.put(KnittingTable.Cols.RATING, knitting.rating)
            if (knitting.category != null) {
                values.put(KnittingTable.Cols.CATEGORY_ID, knitting.category.id)
            } else {
                values.putNull(KnittingTable.Cols.CATEGORY_ID)
            }
            values.put(KnittingTable.Cols.STATUS, knitting.status.name)

            val id = database.insert(KnittingTable.KNITTINGS, null, values)
            debug("Added knitting $knitting to database, id=$id")
        }
    }

    /**
     * Updates a knitting in the database
     *
     * @param knitting knitting that should be updated
     * @return updated knitting
     */
    @Synchronized
    fun updateKnitting(knitting: Knitting): Knitting {
        debug("Updating knitting " + knitting + ", default photo: " + knitting.defaultPhoto)
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(KnittingTable.Cols.TITLE, knitting.title)
            values.put(KnittingTable.Cols.DESCRIPTION, knitting.description)
            values.put(KnittingTable.Cols.STARTED, knitting.started.time)
            if (knitting.finished != null) {
                values.put(KnittingTable.Cols.FINISHED, knitting.finished.time)
            }
            values.put(KnittingTable.Cols.NEEDLE_DIAMETER, knitting.needleDiameter)
            values.put(KnittingTable.Cols.SIZE, knitting.size)
            if (knitting.defaultPhoto != null) {
                debug("Default photo: " + knitting.defaultPhoto)
                values.put(KnittingTable.Cols.DEFAULT_PHOTO_ID, knitting.defaultPhoto.id)
            } else {
                values.putNull(KnittingTable.Cols.DEFAULT_PHOTO_ID)
            }
            values.put(KnittingTable.Cols.RATING, knitting.rating)
            values.put(KnittingTable.Cols.DURATION, knitting.duration)
            if (knitting.category != null) {
                values.put(KnittingTable.Cols.CATEGORY_ID, knitting.category.id)
            } else {
                values.putNull(KnittingTable.Cols.CATEGORY_ID)
            }
            values.put(KnittingTable.Cols.STATUS, knitting.status.name)

            database.update(KnittingTable.KNITTINGS,
                    values,
                    KnittingTable.Cols.ID + "=" + knitting.id, null)

            val cursor = database.query(KnittingTable.KNITTINGS,
                    KnittingTable.Columns, KnittingTable.Cols.ID + "=" + knitting.id, null, null, null, null)

            cursor.moveToFirst()
            val result = cursorToKnitting(cursor)
            cursor.close()

            return result
        }
    }

    /**
     * Deletes the given knitting from the database. All photos for the deleted knitting are also deleted
     *
     * @param knitting knitting that should be deleted
     */
    @Synchronized
    fun deleteKnitting(knitting: Knitting) {
        val id = knitting.id
        // delete all photos from the database
        deleteAllPhotos(knitting)
        dbHelper.writableDatabase.use { database ->
            database.delete(KnittingTable.KNITTINGS, KnittingTable.Cols.ID + "=" + id, null)
            debug("Deleted knitting $id: $knitting")
        }
    }

    /**
     * Deletes all knittings from the database
     */
    @Synchronized
    fun deleteAllKnittings() {
        for (knitting in allKnittings) {
            deleteKnitting(knitting)
        }
    }

    /**
     * Gets the knitting with the given id from the database
     *
     * @param id id of the knitting that should be read from database
     * @return knitting for the given id
     */
    @Synchronized
    fun getKnitting(id: Long): Knitting {
        debug("Getting knitting for id $id")
        dbHelper.readableDatabase.use { database ->
            val cursor = database.query(KnittingTable.KNITTINGS,
                    KnittingTable.Columns, KnittingTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val knitting = cursorToKnitting(cursor)
            cursor.close()

            return knitting
        }
    }

    @Synchronized
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
    @Synchronized
    fun getPhoto(id: Long): Photo {
        debug("Getting photo for id $id")
        dbHelper.readableDatabase.use { database ->
            val cursor = database.query(PhotoTable.PHOTOS,
                    PhotoTable.Columns, PhotoTable.Cols.ID + "=" + id, null, null, null, null)

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
    @Synchronized
    fun getAllPhotos(knitting: Knitting): ArrayList<Photo> {
        dbHelper.readableDatabase.use { database ->
            val photos = ArrayList<Photo>()

            val whereClause = PhotoTable.Cols.KNITTING_ID + " = ?"
            val whereArgs = arrayOf(java.lang.Double.toString(knitting.id.toDouble()))
            val cursor = database.query(PhotoTable.PHOTOS, PhotoTable.Columns, whereClause, whereArgs, null, null, null)

            cursor.moveToFirst()
            var photo: Photo

            while (!cursor.isAfterLast) {
                photo = cursorToPhoto(cursor)
                photos.add(photo)
                debug("Read photo $photo")
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
    @Synchronized
    fun createPhoto(filename: File, knittingID: Long, preview: Bitmap?, description: String): Photo {
        debug("Creating photo for $filename, knitting id: $knittingID, preview: $preview, description: $description")
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(PhotoTable.Cols.FILENAME, filename.absolutePath)
            values.put(PhotoTable.Cols.KNITTING_ID, knittingID)
            values.put(PhotoTable.Cols.DESCRIPTION, description)
            val previewBytes = Photo.getBytes(preview)
            if (previewBytes != null) {
                values.put(PhotoTable.Cols.PREVIEW, previewBytes)
            } else {
                values.putNull(PhotoTable.Cols.PREVIEW)
            }

            val id = database.insert(PhotoTable.PHOTOS, null, values)

            val cursor = database.query(PhotoTable.PHOTOS,
                    PhotoTable.Columns, PhotoTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val photo = cursorToPhoto(cursor)
            cursor.close()

            return photo
        }
    }

    /**
     * Adds the given photo to the database
     *
     * @param photo photo that should be added to the database
     * @param manualID: use photo ID instead of auto-imcremented id
     */
    @Synchronized
    fun addPhoto(photo: Photo, manualID: Boolean = false) {
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            if (manualID) {
                values.put(PhotoTable.Cols.ID, photo.id)
            }
            values.put(PhotoTable.Cols.FILENAME, photo.filename.absolutePath)
            values.put(PhotoTable.Cols.KNITTING_ID, photo.knittingID)
            values.put(PhotoTable.Cols.DESCRIPTION, photo.description)
            val previewBytes = Photo.getBytes(photo.preview)
            if (previewBytes != null) {
                values.put(PhotoTable.Cols.PREVIEW, previewBytes)
            } else {
                values.putNull(PhotoTable.Cols.PREVIEW)
            }

            val id = database.insert(PhotoTable.PHOTOS, null, values)
            debug("Added photo $photo to database, id=$id")
        }
    }

    /**
     * Updates a photo in the database
     *
     * @param photo photo that should be updated
     * @return updated photo
     */
    @Synchronized
    fun updatePhoto(photo: Photo): Photo {
        debug("Updating photo $photo")
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(PhotoTable.Cols.FILENAME, photo.filename.absolutePath)
            values.put(PhotoTable.Cols.KNITTING_ID, photo.knittingID)
            values.put(PhotoTable.Cols.DESCRIPTION, photo.description)
            val previewBytes = Photo.getBytes(photo.preview)
            if (previewBytes != null) {
                values.put(PhotoTable.Cols.PREVIEW, previewBytes)
            } else {
                values.putNull(PhotoTable.Cols.PREVIEW)
            }

            database.update(PhotoTable.PHOTOS,
                    values,
                    PhotoTable.Cols.ID + "=" + photo.id, null)

            val cursor = database.query(PhotoTable.PHOTOS,
                    PhotoTable.Columns, PhotoTable.Cols.ID + "=" + photo.id, null, null, null, null)

            cursor.moveToFirst()
            val result = cursorToPhoto(cursor)
            cursor.close()

            return result
        }
    }

    /**
     * Deletes the given photo from the database. The photo file is also deleted.
     *
     * @param photo photo that should be deleted
     */
    @Synchronized
    fun deletePhoto(photo: Photo) {
        deletePhotoFile(photo.filename)
        val id = photo.id
        dbHelper.writableDatabase.use { database ->
            database.delete(PhotoTable.PHOTOS, PhotoTable.Cols.ID + "=" + id, null)
            debug("Deleted photo $id: $photo")
        }
    }

    /**
     * Delete all photos for the given knitting. Photo filres are also deleted.
     *
     * @param knitting knitting to delete photos for
     */
    @Synchronized
    private fun deleteAllPhotos(knitting: Knitting) {
        for(photo in getAllPhotos(knitting)) {
            deletePhotoFile(photo.filename)
        }
        val id = knitting.id
        dbHelper.writableDatabase.use { database ->
            val whereClause = PhotoTable.Cols.KNITTING_ID + "= ?"
            val whereArgs = arrayOf(java.lang.Long.toString(id))
            database.delete(PhotoTable.PHOTOS, whereClause, whereArgs)
            debug("Removed knitting $id: $knitting")
        }
    }

    /**
     * Deletes all photos from the database. Photo files are also deleted.
     */
    @Synchronized
    fun deleteAllPhotos() {
        for (photo in allPhotos) {
            deletePhoto(photo)
        }
    }

    /**
     * Gets the category with the given id from the database
     *
     * @param id id of the category that should be read from database
     * @return category for the given id
     */
    @Synchronized
    fun getCategory(id: Long): Category {
        debug("Getting category for id $id")
        dbHelper.readableDatabase.use { database ->
            val cursor = database.query(CategoryTable.CATEGORY,
                    CategoryTable.Columns, CategoryTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val category = cursorToCategory(cursor)
            cursor.close()

            return category
        }
    }

    /**
     * Creates a new category and adds it to the database
     *
     * @param name name of the category
     * @param color color used for the category
     * @return new category
     */
    @Synchronized
    fun createCategory(name: String, color: Int?): Category {
        debug("Creating category $name, color: $color")
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(CategoryTable.Cols.NAME, name)
            if (color != null) {
                values.put(CategoryTable.Cols.COLOR, color)
            } else {
                values.putNull(CategoryTable.Cols.COLOR)
            }

            val id = database.insert(CategoryTable.CATEGORY, null, values)

            val cursor = database.query(CategoryTable.CATEGORY,
                    CategoryTable.Columns, CategoryTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val category = cursorToCategory(cursor)
            cursor.close()

            return category
        }
    }

    /**
     * Adds the given category to the database
     *
     * @param category category that should be added to the database
     * @param manualID: use cagegpry ID instead of auto-imcremented id
     */
    @Synchronized
    fun addCategory(category: Category, manualID: Boolean = false) {
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            if (manualID) {
                values.put(CategoryTable.Cols.ID, category.id)
            }
            values.put(CategoryTable.Cols.NAME, category.name)
            if (category.color != null) {
                values.put(CategoryTable.Cols.COLOR, category.color)
            } else {
                values.putNull(CategoryTable.Cols.COLOR)
            }
            val id = database.insert(CategoryTable.CATEGORY, null, values)
            debug("Added category $category to database, id=$id")
        }
    }

    /**
     * Updates a photo in the database
     *
     * @param category category that should be updated
     * @return updated category
     */
    @Synchronized
    fun updateCategory(category: Category): Category {
        debug("Updating category $category")
        dbHelper.writableDatabase.use { database ->
            val values = ContentValues()
            values.put(CategoryTable.Cols.NAME, category.name)
            if (category.color != null) {
                values.put(CategoryTable.Cols.COLOR, category.color)
            } else {
                values.putNull(CategoryTable.Cols.COLOR)
            }

            database.update(CategoryTable.CATEGORY,
                    values,
                    CategoryTable.Cols.ID + "=" + category.id, null)

            val cursor = database.query(CategoryTable.CATEGORY,
                    CategoryTable.Columns, CategoryTable.Cols.ID + "=" + category.id, null, null, null, null)

            cursor.moveToFirst()
            val result = cursorToCategory(cursor)
            cursor.close()

            return result
        }
    }

    /**
     * Deletes the given category from the database
     *
     * @param category category that should be deleted
     */
    @Synchronized
    fun deleteCategory(category: Category) {
        // get all knittings from the database and remove the category we are going to delete
        // we need to do this before deleting the category
        val knittings = allKnittings.filter { it.category == category }
        for (knitting in knittings) {
            updateKnitting(knitting.copy(category = null))
        }
        debug("Removed category " + category.id + " from " + knittings.size + "knittings")
        dbHelper.writableDatabase.use { database ->
            // delete the category
            database.delete(CategoryTable.CATEGORY, CategoryTable.Cols.ID + "=" + category.id, null)
            debug("Deleted category " + category.id + ": " + category)
        }
    }


    @Synchronized
    fun deleteAllCategories() {
        for (category in allCategories) {
            deleteCategory(category)
        }
    }

    /**
     * Gets the category with the given id from the database
     *
     * @param id id of the category that should be read from database
     * @return category for the given id
     */
    @Synchronized
    fun getNeedle(id: Long): Needle {
        debug("Getting needle for id $id")
        dbHelper.readableDatabase.use { database ->
            val cursor = database.query(NeedleTable.NEEDLES,
                    NeedleTable.Columns, NeedleTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val needle = cursorToNeedle(context, cursor)
            cursor.close()

            return needle
        }
    }

    /**
     * Creates a new needle and adds it to the database
     *
     * @param name name of the needle
     * @return new needle
     */
    @Synchronized
    fun createNeedle(needle: Needle): Needle {
        debug("Creating needle ${needle.name}")
        dbHelper.writableDatabase.use { database ->
            val values = NeedleTable.createContentValues(needle)
            val id = database.insert(NeedleTable.NEEDLES, null, values)

            val cursor = database.query(NeedleTable.NEEDLES,
                    NeedleTable.Columns, NeedleTable.Cols.ID + "=" + id, null, null, null, null)

            cursor.moveToFirst()
            val result = cursorToNeedle(context, cursor)
            cursor.close()

            return result
        }
    }

    /**
     * Adds the given needle to the database
     *
     * @param needle needle that should be added to the database
     * @param manualID: use cagegpry ID instead of auto-imcremented id
     */
    @Synchronized
    fun addNeedle(needle: Needle, manualID: Boolean = false) {
        dbHelper.writableDatabase.use { database ->
            val values = NeedleTable.createContentValues(needle, manualID)
            val id = database.insert(NeedleTable.NEEDLES, null, values)
            debug("Added category $needle to database, id=$id")
        }
    }

    /**
     * Updates a photo in the database
     *
     * @param needle needle that should be updated
     * @return updated needle
     */
    @Synchronized
    fun updateNeedle(needle: Needle): Needle {
        debug("Updating needle $needle")
        dbHelper.writableDatabase.use { database ->
            val values = NeedleTable.createContentValues(needle)
            database.update(NeedleTable.NEEDLES,
                    values,
                    NeedleTable.Cols.ID + "=" + needle.id, null)

            val cursor = database.query(NeedleTable.NEEDLES,
                    NeedleTable.Columns, NeedleTable.Cols.ID + "=" + needle.id, null, null, null, null)

            cursor.moveToFirst()
            val result = cursorToNeedle(context, cursor)
            cursor.close()

            return result
        }
    }

    /**
     * Deletes the given needle from the database
     *
     * @param needle needle that should be deleted
     */
    @Synchronized
    fun deleteNeedle(needle: Needle) {
        dbHelper.writableDatabase.use { database ->
            // delete the category
            database.delete(NeedleTable.NEEDLES, NeedleTable.Cols.ID + "=" + needle.id, null)
            debug("Deleted needle " + needle.id + ": " + needle)
        }
    }


    @Synchronized
    fun deleteAllNeedles() {
        for (needle in allNeedles) {
            deleteNeedle(needle)
        }
    }


    @Synchronized
    private fun cursorToKnitting(cursor: Cursor): Knitting {
        val idIndex = cursor.getColumnIndex(KnittingTable.Cols.ID)
        val idTitle = cursor.getColumnIndex(KnittingTable.Cols.TITLE)
        val idDescription = cursor.getColumnIndex(KnittingTable.Cols.DESCRIPTION)
        val idStarted = cursor.getColumnIndex(KnittingTable.Cols.STARTED)
        val idFinished = cursor.getColumnIndex(KnittingTable.Cols.FINISHED)
        val idNeedleDiameter = cursor.getColumnIndex(KnittingTable.Cols.NEEDLE_DIAMETER)
        val idSize = cursor.getColumnIndex(KnittingTable.Cols.SIZE)
        val idDefaultPhoto = cursor.getColumnIndex(KnittingTable.Cols.DEFAULT_PHOTO_ID)
        val idRating = cursor.getColumnIndex(KnittingTable.Cols.RATING)
        val idDuration = cursor.getColumnIndex(KnittingTable.Cols.DURATION)
        val idCategory = cursor.getColumnIndex(KnittingTable.Cols.CATEGORY_ID)
        val idStatus = cursor.getColumnIndex(KnittingTable.Cols.STATUS)

        val id = cursor.getLong(idIndex)
        val title = cursor.getString(idTitle)
        val description = cursor.getString(idDescription)
        val started = Date(cursor.getLong(idStarted))
        val finished = if (cursor.isNull(idFinished)) null else Date(cursor.getLong(idFinished))
        val needleDiameter = cursor.getString(idNeedleDiameter)
        val size = cursor.getString(idSize)
        val rating = cursor.getDouble(idRating)
        val duration = cursor.getLong(idDuration)

        var defaultPhoto: Photo? = null
        if (!cursor.isNull(idDefaultPhoto)) {
            val defaultPhotoID = cursor.getLong(idDefaultPhoto)
            defaultPhoto = getPhoto(defaultPhotoID)
        }
        var category: Category? = null
        if (!cursor.isNull(idCategory)) {
            val categoryID = cursor.getLong(idCategory)
            category = getCategory(categoryID)
        }
        val status = if (cursor.isNull(idStatus)) {
            Status.PLANNED
        } else {
            val statusStr = cursor.getString(idStatus)
            try {
                Status.valueOf(statusStr)
            } catch (ex: Exception) {
                Status.parse(context, statusStr)
            }
        }
        return Knitting(id, title = title, description = description, started = started, finished = finished, needleDiameter = needleDiameter,
                size = size, rating = rating, defaultPhoto = defaultPhoto, duration = duration, category = category, status = status)
    }

    @Synchronized
    private fun deletePhotoFile(file: File) {
        if (file.exists()) {
            if(file.delete()) {
                debug("Deleted photo $file")
            } else {
                error("Could not delete $file")
            }
        } else {
            error("Could not delete $file}, file does not exist")
        }
    }

    companion object {

        private var sKnittingsDataSource: KnittingsDataSource? = null

        fun getInstance(c: Context?): KnittingsDataSource {
            if (sKnittingsDataSource == null) {
                if (c == null) {
                    throw IllegalArgumentException("context must not be null")
                } else {
                    sKnittingsDataSource = KnittingsDataSource(c)
                }
            }
            return sKnittingsDataSource!!
        }
    }
}

// Access property for Context
val Context.datasource: KnittingsDataSource
    @Synchronized
    get() = KnittingsDataSource.getInstance(applicationContext)

val Fragment.datasource: KnittingsDataSource
    @Synchronized
    get() = KnittingsDataSource.getInstance(this.activity)
