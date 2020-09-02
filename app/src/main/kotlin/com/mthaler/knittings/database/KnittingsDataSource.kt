package com.mthaler.knittings.database

import android.content.Context
import android.database.Cursor
import android.os.Environment
import android.util.Log
import java.io.File
import java.util.ArrayList
import java.util.Date
import androidx.fragment.app.Fragment
import com.mthaler.dbapp.database.AbstractObservableDatabase
import com.mthaler.knittings.database.table.*
import com.mthaler.knittings.database.table.CategoryTable.cursorToCategory
import com.mthaler.knittings.database.table.PhotoTable.cursorToPhoto
import com.mthaler.knittings.database.table.NeedleTable.cursorToNeedle
import com.mthaler.knittings.database.table.RowsTable.cursorToRows
import com.mthaler.knittings.model.*
import java.lang.Exception

class KnittingsDataSource private constructor(context: Context) : AbstractObservableDatabase(), KnittingRepository, PhotoRepository {

    private val context: Context = context.applicationContext
    private val dbHelper: KnittingDatabaseHelper

    override val allKnittings: ArrayList<Knitting>
        @Synchronized
        get() = dbHelper.readableDatabase.use { database ->
            val knittings = ArrayList<Knitting>()
            val cursor = database.query(KnittingTable.KNITTINGS, KnittingTable.Columns, null, null, null, null, null)
            cursor.moveToFirst()
            var knitting: Knitting
            while (!cursor.isAfterLast) {
                knitting = cursorToKnitting(cursor)
                knittings.add(knitting)
                Log.d(TAG, "Read knitting " + knitting.id + ", default photo: " + knitting.defaultPhoto)
                cursor.moveToNext()
            }
            cursor.close()
            return knittings
        }

    override val allPhotos: ArrayList<Photo>
        @Synchronized
        get() = dbHelper.readableDatabase.use { database ->
            val photos = ArrayList<Photo>()
            val cursor = database.query(PhotoTable.PHOTOS, PhotoTable.Columns, null, null, null, null, null)
            cursor.moveToFirst()
            var photo: Photo
            while (!cursor.isAfterLast) {
                photo = cursorToPhoto(cursor)
                photos.add(photo)
                Log.d(TAG, "Read photo " + photo.id + " filename: " + photo.filename)
                cursor.moveToNext()
            }
            cursor.close()
            return photos
        }

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
                Log.d(TAG, "Read category $category")
                cursor.moveToNext()
            }
            cursor.close()
            return categories
        }

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
                Log.d(TAG, "Read category $needle")
                cursor.moveToNext()
            }
            cursor.close()
            return needles
        }

    init {
        dbHelper = context.database
    }

    @Synchronized
    override fun addKnitting(knitting: Knitting, manualID: Boolean): Knitting {
        dbHelper.writableDatabase.use { database ->
            val values = KnittingTable.createContentValues(knitting, manualID)
            val id = database.insert(KnittingTable.KNITTINGS, null, values)
            val cursor = database.query(KnittingTable.KNITTINGS,
                    KnittingTable.Columns, KnittingTable.Cols.ID + "=" + id, null, null, null, null)
            cursor.moveToFirst()
            val knittings = cursorToKnitting(cursor)
            cursor.close()
            notifyObservers()
            return knittings
        }
    }

    @Synchronized
    override fun updateKnitting(knitting: Knitting): Knitting {
        val result = updateKnittingImpl(knitting)
        notifyObservers()
        return result
    }

    private fun updateKnittingImpl(knitting: Knitting): Knitting {
        Log.d(TAG, "Updating knitting " + knitting + ", default photo: " + knitting.defaultPhoto)
        dbHelper.writableDatabase.use { database ->
            val values = KnittingTable.createContentValues(knitting)
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

    @Synchronized
    override fun deleteKnitting(knitting: Knitting) {
        deleteKnittingImpl(knitting)
        notifyObservers()
    }

    @Synchronized
    override fun deleteAllKnittings() {
        for (knitting in allKnittings) {
            deleteKnittingImpl(knitting)
        }
        notifyObservers()
    }

    private fun deleteKnittingImpl(knitting: Knitting) {
        val id = knitting.id
        // delete all photos from the database
        deleteAllPhotos(knitting)
        dbHelper.writableDatabase.use { database ->
            database.delete(KnittingTable.KNITTINGS, KnittingTable.Cols.ID + "=" + id, null)
            Log.d(TAG, "Deleted knitting $id: $knitting")
        }
    }

    @Synchronized
    override fun getKnitting(id: Long): Knitting {
        Log.d(TAG, "Getting knitting for id $id")
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
    override fun getPhotoFile(knitting: Knitting): File? {
        val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return if (externalFilesDir != null) File(externalFilesDir, knitting.photoFilename) else null
    }

    @Synchronized
    override fun getPhoto(id: Long): Photo {
        Log.d(TAG, "Getting photo for id $id")
        dbHelper.readableDatabase.use { database ->
            val cursor = database.query(PhotoTable.PHOTOS,
                    PhotoTable.Columns, PhotoTable.Cols.ID + "=" + id, null, null, null, null)
            cursor.moveToFirst()
            val photo = cursorToPhoto(cursor)
            cursor.close()
            return photo
        }
    }

    @Synchronized
    override fun getAllPhotos(knitting: Knitting): ArrayList<Photo> {
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
                Log.d(TAG, "Read photo $photo")
                cursor.moveToNext()
            }
            cursor.close()
            return photos
        }
    }

    @Synchronized
    override fun addPhoto(photo: Photo, manualID: Boolean): Photo {
        dbHelper.writableDatabase.use { database ->
            val values = PhotoTable.createContentValues(photo, manualID)
            val id = database.insert(PhotoTable.PHOTOS, null, values)
            val cursor = database.query(PhotoTable.PHOTOS,
                    PhotoTable.Columns, PhotoTable.Cols.ID + "=" + id, null, null, null, null)
            cursor.moveToFirst()
            val result = cursorToPhoto(cursor)
            cursor.close()
            notifyObservers()
            return result
        }
    }

    @Synchronized
    override fun updatePhoto(photo: Photo): Photo {
        Log.d(TAG, "Updating photo $photo")
        dbHelper.writableDatabase.use { database ->
            val values = PhotoTable.createContentValues(photo)
            database.update(PhotoTable.PHOTOS,
                    values,
                    PhotoTable.Cols.ID + "=" + photo.id, null)
            val cursor = database.query(PhotoTable.PHOTOS,
                    PhotoTable.Columns, PhotoTable.Cols.ID + "=" + photo.id, null, null, null, null)
            cursor.moveToFirst()
            val result = cursorToPhoto(cursor)
            cursor.close()
            notifyObservers()
            return result
        }
    }

    @Synchronized
    override fun deletePhoto(photo: Photo) {
        val knitting = getKnitting(photo.knittingID)
        if (knitting.defaultPhoto != null && knitting.defaultPhoto.id == photo.id) {
            val photos = getAllPhotos(knitting).filter { it.id != photo.id }.sortedBy { it.id }
            if (photos.isNotEmpty()) {
                updateKnittingImpl(knitting.copy(defaultPhoto = photos.last()))
            } else {
                updateKnittingImpl(knitting.copy(defaultPhoto = null))
            }
        }
        deletePhotoImpl(photo)
        notifyObservers()
    }

    @Synchronized
    private fun deleteAllPhotos(knitting: Knitting) {
        for (photo in getAllPhotos(knitting)) {
            deletePhotoFile(photo.filename)
        }
        val id = knitting.id
        dbHelper.writableDatabase.use { database ->
            val whereClause = PhotoTable.Cols.KNITTING_ID + "= ?"
            val whereArgs = arrayOf(java.lang.Long.toString(id))
            database.delete(PhotoTable.PHOTOS, whereClause, whereArgs)
            Log.d(TAG, "Removed knitting $id: $knitting")
        }
    }

    @Synchronized
    override fun deleteAllPhotos() {
        for (photo in allPhotos) {
            deletePhotoImpl(photo)
        }
        notifyObservers()
    }

    private fun deletePhotoImpl(photo: Photo) {
        deletePhotoFile(photo.filename)
        val id = photo.id
        dbHelper.writableDatabase.use { database ->
            database.delete(PhotoTable.PHOTOS, PhotoTable.Cols.ID + "=" + id, null)
            Log.d(TAG, "Deleted photo $id: $photo")
        }
    }

    @Synchronized
    fun getCategory(id: Long): Category {
        Log.d(TAG, "Getting category for id $id")
        dbHelper.readableDatabase.use { database ->
            val cursor = database.query(CategoryTable.CATEGORY,
                    CategoryTable.Columns, CategoryTable.Cols.ID + "=" + id, null, null, null, null)
            cursor.moveToFirst()
            val category = cursorToCategory(cursor)
            cursor.close()
            return category
        }
    }

    @Synchronized
    fun addCategory(category: Category, manualID: Boolean = false): Category {
        dbHelper.writableDatabase.use { database ->
            val values = CategoryTable.createContentValues(category, manualID)
            val id = database.insert(CategoryTable.CATEGORY, null, values)
            val cursor = database.query(CategoryTable.CATEGORY,
                    CategoryTable.Columns, CategoryTable.Cols.ID + "=" + id, null, null, null, null)
            cursor.moveToFirst()
            val result = cursorToCategory(cursor)
            cursor.close()
            notifyObservers()
            return result
        }
    }

    @Synchronized
    fun updateCategory(category: Category): Category {
        Log.d(TAG, "Updating category $category")
        dbHelper.writableDatabase.use { database ->
            val values = CategoryTable.createContentValues(category)
            database.update(CategoryTable.CATEGORY,
                    values,
                    CategoryTable.Cols.ID + "=" + category.id, null)
            val cursor = database.query(CategoryTable.CATEGORY,
                    CategoryTable.Columns, CategoryTable.Cols.ID + "=" + category.id, null, null, null, null)
            cursor.moveToFirst()
            val result = cursorToCategory(cursor)
            cursor.close()
            notifyObservers()
            return result
        }
    }

    @Synchronized
    fun deleteCategory(category: Category) {
        deleteCategoryImpl(category)
        notifyObservers()
    }

    @Synchronized
    fun deleteAllCategories() {
        for (category in allCategories) {
            deleteCategoryImpl(category)
        }
        notifyObservers()
    }

    private fun deleteCategoryImpl(category: Category) {
        // get all knittings from the database and remove the category we are going to delete
        // we need to do this before deleting the category
        val knittings = allKnittings.filter { it.category == category }
        for (knitting in knittings) {
            updateKnitting(knitting.copy(category = null))
        }
        Log.d(TAG, "Removed category " + category.id + " from " + knittings.size + "knittings")
        dbHelper.writableDatabase.use { database ->
            // delete the category
            database.delete(CategoryTable.CATEGORY, CategoryTable.Cols.ID + "=" + category.id, null)
            Log.d(TAG, "Deleted category " + category.id + ": " + category)
        }
    }

    @Synchronized
    fun getNeedle(id: Long): Needle {
        Log.d(TAG, "Getting needle for id $id")
        dbHelper.readableDatabase.use { database ->
            val cursor = database.query(NeedleTable.NEEDLES,
                    NeedleTable.Columns, NeedleTable.Cols.ID + "=" + id, null, null, null, null)
            cursor.moveToFirst()
            val needle = cursorToNeedle(context, cursor)
            cursor.close()
            return needle
        }
    }

    @Synchronized
    fun addNeedle(needle: Needle, manualID: Boolean = false): Needle {
        dbHelper.writableDatabase.use { database ->
            val values = NeedleTable.createContentValues(needle, manualID)
            val id = database.insert(NeedleTable.NEEDLES, null, values)
            val cursor = database.query(NeedleTable.NEEDLES,
                    NeedleTable.Columns, NeedleTable.Cols.ID + "=" + id, null, null, null, null)
            cursor.moveToFirst()
            val result = cursorToNeedle(context, cursor)
            cursor.close()
            notifyObservers()
            return result
        }
    }

    @Synchronized
    fun updateNeedle(needle: Needle): Needle {
        Log.d(TAG, "Updating needle $needle")
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
            notifyObservers()
            return result
        }
    }

    @Synchronized
    fun deleteNeedle(needle: Needle) {
        deleteNeedleImpl(needle)
        notifyObservers()
    }

    @Synchronized
    fun deleteAllNeedles() {
        for (needle in allNeedles) {
            deleteNeedleImpl(needle)
        }
        notifyObservers()
    }

    private fun deleteNeedleImpl(needle: Needle) {
        dbHelper.writableDatabase.use { database ->
            // delete the category
            database.delete(NeedleTable.NEEDLES, NeedleTable.Cols.ID + "=" + needle.id, null)
            Log.d(TAG, "Deleted needle " + needle.id + ": " + needle)
        }
        notifyObservers()
    }

    @Synchronized
    fun getRows(id: Long): Rows {
        Log.d(TAG, "Getting rows for id $id")
        dbHelper.readableDatabase.use { database ->
            val cursor = database.query(RowsTable.ROWS,
                    NeedleTable.Columns, RowsTable.Cols.ID + "=" + id, null, null, null, null)
            cursor.moveToFirst()
            val rows = cursorToRows(cursor)
            cursor.close()
            return rows
        }
    }

    @Synchronized
    fun getRows(knitting: Knitting): Rows? {
        dbHelper.readableDatabase.use { database ->
            val whereClause = RowsTable.Cols.KNITTING_ID + " = ?"
            val whereArgs = arrayOf(java.lang.Double.toString(knitting.id.toDouble()))
            val cursor = database.query(RowsTable.ROWS, RowsTable.Columns, whereClause, whereArgs, null, null, null)
            cursor.moveToFirst()
            if (!cursor.isAfterLast) {
                return cursorToRows(cursor)
            } else {
                return null
            }
        }
    }

    @Synchronized
    fun addRows(rows: Rows, manualID: Boolean = false): Rows {
        dbHelper.writableDatabase.use { database ->
            val values = RowsTable.createContentValues(rows, manualID)
            val id = database.insert(RowsTable.ROWS, null, values)
            val cursor = database.query(RowsTable.ROWS,
                    RowsTable.Columns, RowsTable.Cols.ID + "=" + id, null, null, null, null)
            cursor.moveToFirst()
            val result = cursorToRows(cursor)
            cursor.close()
            notifyObservers()
            return result
        }
    }

    @Synchronized
    fun updateRows(rows: Rows): Rows {
        Log.d(TAG, "Updating rows $rows")
        dbHelper.writableDatabase.use { database ->
            val values = RowsTable.createContentValues(rows)
            database.update(RowsTable.ROWS,
                    values,
                    RowsTable.Cols.ID + "=" + rows.id, null)
            val cursor = database.query(RowsTable.ROWS,
                    RowsTable.Columns, RowsTable.Cols.ID + "=" + rows.id, null, null, null, null)
            cursor.moveToFirst()
            val result = cursorToRows(cursor)
            cursor.close()
            notifyObservers()
            return result
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
            if (file.delete()) {
                Log.d(TAG, "Deleted photo $file")
            } else {
                Log.e(TAG, "Could not delete $file")
            }
        } else {
            Log.e(TAG, "Could not delete $file}, file does not exist")
        }
    }

    companion object {

        private const val TAG = "KnittingsDataSource"

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
