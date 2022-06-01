package com.mthaler.knittings.database

import android.database.Cursor
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.room.getQueryDispatcher
import java.io.File
import java.util.ArrayList
import java.util.Date
import com.mthaler.knittings.database.table.CategoryTable
import com.mthaler.knittings.database.table.*
import com.mthaler.knittings.R
import com.mthaler.knittings.model.*
import java.lang.Exception

object KnittingsDataSource : AbstractObservableDatabase(), PhotoDataSource, CategoryDataSource, ProjectsDataSource<Knitting> {

    private const val TAG = "KnittingsDataSource"

    private lateinit var db: AppDatabase

    override val allProjects: List<Knitting>
        get() = db.knittingDao().getAll()

    override val allPhotos: List<Photo>
        get() = db.photoDao().getAll()

    override val allCategories: List<Category>
        get() = db.categoryDao().getAll()

    val allNeedles: List<Needle>
        get() = db.needleDao().getAll()

    val allRowCounters: List<RowCounter>
        get() = db.rowCounterDao().getAll()

    override fun addProject(project: Knitting, manualID: Boolean): Knitting {
         val id = db.knittingDao().insert(project)
         notifyObservers()
         return project.copy(id = id)
    }

    override fun updateProject(project: Knitting): Knitting {
        val result = updateKnittingImpl(project)
        notifyObservers()
        return result
    }

    private fun updateKnittingImpl(knitting: Knitting): Knitting {
        Log.d(TAG, "Updating knitting " + knitting + ", default photo: " + knitting.defaultPhoto)
        val id = db.knittingDao().insert(knitting)
        return knitting.copy(id = id)
    }

    override fun deleteProject(project: Knitting) {
        deleteKnittingImpl(project)
        notifyObservers()
    }

    override fun deleteAllProjects() {
        for (knitting in allProjects) {
            deleteKnittingImpl(knitting)
        }
        notifyObservers()
    }

    private fun deleteKnittingImpl(knitting: Knitting) {
        // delete all photos from the database
        deleteAllPhotos(knitting)
        deleteAllRowCounters(knitting)
        db.knittingDao().delete(knitting)
    }

    override fun getProject(id: Long): Knitting {
        Log.d(TAG, "Getting knittinh for id $id")
        return db.knittingDao().get(id)
    }

    override fun getPhoto(id: Long): Photo {
        Log.d(TAG, "Getting photo for id $id")
        return db.photoDao().get(id)
    }

    override fun getAllPhotos(id: Long): List<Photo> = db.photoDao().getAll()

    override fun addPhoto(photo: Photo): Photo {
        val id = db.photoDao().insert(photo)
        notifyObservers()
        return photo.copy(id = id)
    }

    @Synchronized
    override fun updatePhoto(photo: Photo): Photo {
        Log.d(TAG, "Updating photo $photo")
        context.database.writableDatabase.use { database ->
            val values = PhotoTable.createContentValues(photo)
            database.update(PhotoTable.PHOTOS,
                    values,
                    PhotoTable.Cols.ID + "=" + photo.id, null)
            val cursor = database.query(PhotoTable.PHOTOS,
                    PhotoTable.Columns, PhotoTable.Cols.ID + "=" + photo.id, null, null, null, null)
            val result = cursor.first(PhotoConverter::convert)
            notifyObservers()
            return result
        }
    }

    @Synchronized
    override fun deletePhoto(photo: Photo) {
        val knitting = getProject(photo.ownerID)
        if (knitting.defaultPhoto != null && knitting.defaultPhoto.id == photo.id) {
            val photos = getAllPhotos(knitting.id).filter { it.id != photo.id }.sortedBy { it.id }
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
        for (photo in getAllPhotos(knitting.id)) {
            deletePhotoFile(photo.filename)
        }
        val id = knitting.id
        context.database.writableDatabase.use { database ->
            val whereClause = PhotoTable.Cols.KNITTING_ID + "= ?"
            val whereArgs = arrayOf(id.toString())
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
        context.database.writableDatabase.use { database ->
            database.delete(PhotoTable.PHOTOS, PhotoTable.Cols.ID + "=" + id, null)
            Log.d(TAG, "Deleted photo $id: $photo")
        }
    }

    @Synchronized
    override fun setDefaultPhoto(ownerID: Long, photo: Photo) {
        val knitting = getProject(ownerID)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val useNewestAsPreview = prefs.getBoolean(context.resources.getString(R.string.key_photos_use_newest_as_preview), true)
        if (useNewestAsPreview) {
            updateKnittingImpl(knitting.copy(defaultPhoto = photo))
        } else {
            if (knitting.defaultPhoto == null) {
                updateKnittingImpl(knitting.copy(defaultPhoto = photo))
            }
        }
        notifyObservers()
    }

    @Synchronized
    override fun getCategory(id: Long): Category? {
        Log.d(TAG, "Getting category for id $id")
        context.database.readableDatabase.use { database ->
            val cursor = database.query(
                CategoryTable.CATEGORY,
                    CategoryTable.Columns, CategoryTable.Cols.ID + "=" + id, null, null, null, null)
            return cursor.firstOrNull(CategoryConverter::convert)
        }
    }

    @Synchronized
    override fun addCategory(category: Category, manualID: Boolean): Category {
        context.database.writableDatabase.use { database ->
            val values = CategoryTable.createContentValues(category, manualID)
            val id = database.insert(CategoryTable.CATEGORY, null, values)
            val cursor = database.query(
                CategoryTable.CATEGORY,
                    CategoryTable.Columns, CategoryTable.Cols.ID + "=" + id, null, null, null, null)
            val result = cursor.first(CategoryConverter::convert)
            notifyObservers()
            return result
        }
    }

    @Synchronized
    override fun updateCategory(category: Category): Category {
        Log.d(TAG, "Updating category $category")
        context.database.writableDatabase.use { database ->
            val values = CategoryTable.createContentValues(category)
            database.update(
                CategoryTable.CATEGORY,
                    values,
                    CategoryTable.Cols.ID + "=" + category.id, null)
            val cursor = database.query(
                CategoryTable.CATEGORY,
                    CategoryTable.Columns, CategoryTable.Cols.ID + "=" + category.id, null, null, null, null)
            val result = cursor.first(CategoryConverter::convert)
            notifyObservers()
            return result
        }
    }

    @Synchronized
    override fun deleteCategory(category: Category) {
        deleteCategoryImpl(category)
        notifyObservers()
    }

    @Synchronized
    override fun deleteAllCategories() {
        for (category in allCategories) {
            deleteCategoryImpl(category)
        }
        notifyObservers()
    }

    private fun deleteCategoryImpl(category: Category) {
        // get all knittings from the database and remove the category we are going to delete
        // we need to do this before deleting the category
        val knittings = allProjects.filter { it.category == category }
        for (knitting in knittings) {
            updateProject(knitting.copy(category = null))
        }
        Log.d(TAG, "Removed category " + category.id + " from " + knittings.size + "knittings")
        context.database.writableDatabase.use { database ->
            // delete the category
            database.delete(CategoryTable.CATEGORY, CategoryTable.Cols.ID + "=" + category.id, null)
            Log.d(TAG, "Deleted category " + category.id + ": " + category)
        }
    }

    @Synchronized
    fun getNeedle(id: Long): Needle {
        Log.d(TAG, "Getting needle for id $id")
        context.database.readableDatabase.use { database ->
            val cursor = database.query(NeedleTable.NEEDLES,
                    NeedleTable.Columns, NeedleTable.Cols.ID + "=" + id, null, null, null, null)
            val converter = NeedleConverter(context)
            return cursor.first(converter::convert)
        }
    }

    @Synchronized
    fun addNeedle(needle: Needle, manualID: Boolean = false): Needle {
        context.database.writableDatabase.use { database ->
            val values = NeedleTable.createContentValues(needle, manualID)
            val id = database.insert(NeedleTable.NEEDLES, null, values)
            val cursor = database.query(NeedleTable.NEEDLES,
                    NeedleTable.Columns, NeedleTable.Cols.ID + "=" + id, null, null, null, null)
            val converter = NeedleConverter(context)
            val result = cursor.first(converter::convert)
            notifyObservers()
            return result
        }
    }

    @Synchronized
    fun updateNeedle(needle: Needle): Needle {
        Log.d(TAG, "Updating needle $needle")
        context.database.writableDatabase.use { database ->
            val values = NeedleTable.createContentValues(needle)
            database.update(NeedleTable.NEEDLES,
                    values,
                    NeedleTable.Cols.ID + "=" + needle.id, null)
            val cursor = database.query(NeedleTable.NEEDLES,
                    NeedleTable.Columns, NeedleTable.Cols.ID + "=" + needle.id, null, null, null, null)
            val converter = NeedleConverter(context)
            val result = cursor.first(converter::convert)
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
        context.database.writableDatabase.use { database ->
            // delete the category
            database.delete(NeedleTable.NEEDLES, NeedleTable.Cols.ID + "=" + needle.id, null)
            Log.d(TAG, "Deleted needle " + needle.id + ": " + needle)
        }
        notifyObservers()
    }

    @Synchronized
    fun getRowCounter(id: Long): RowCounter {
        Log.d(TAG, "Getting row counter for id $id")
        context.database.readableDatabase.use { database ->
            val cursor = database.query(RowCounterTable.ROW_COUNTERS,
                    NeedleTable.Columns, RowCounterTable.Cols.ID + "=" + id, null, null, null, null)
            return  cursor.first(RowCounterConverter::convert)
        }
    }

    @Synchronized
    fun getRowCounter(knitting: Knitting): RowCounter? {
        context.database.readableDatabase.use { database ->
            val whereClause = RowCounterTable.Cols.KNITTING_ID + " = ?"
            val whereArgs = arrayOf(knitting.id.toString())
            val cursor = database.query(RowCounterTable.ROW_COUNTERS, RowCounterTable.Columns, whereClause, whereArgs, null, null, null)
            if (!cursor.isAfterLast) {
                return cursor.first(RowCounterConverter::convert)
            } else {
                return null
            }
        }
    }

    @Synchronized
    fun addRowCounter(rowCounter: RowCounter, manualID: Boolean = false): RowCounter {
        context.database.writableDatabase.use { database ->
            val values = RowCounterTable.createContentValues(rowCounter, manualID)
            val id = database.insert(RowCounterTable.ROW_COUNTERS, null, values)
            val cursor = database.query(RowCounterTable.ROW_COUNTERS,
                    RowCounterTable.Columns, RowCounterTable.Cols.ID + "=" + id, null, null, null, null)
            val result = cursor.first(RowCounterConverter::convert)
            notifyObservers()
            return result
        }
    }

    @Synchronized
    fun updateRowCounter(rowCounter: RowCounter): RowCounter {
        Log.d(TAG, "Updating row counter $rowCounter")
        context.database.writableDatabase.use { database ->
            val values = RowCounterTable.createContentValues(rowCounter)
            database.update(RowCounterTable.ROW_COUNTERS,
                    values,
                    RowCounterTable.Cols.ID + "=" + rowCounter.id, null)
            val cursor = database.query(RowCounterTable.ROW_COUNTERS,
                    RowCounterTable.Columns, RowCounterTable.Cols.ID + "=" + rowCounter.id, null, null, null, null)
            val result = cursor.first(RowCounterConverter::convert)
            notifyObservers()
            return result
        }
    }

    @Synchronized
    private fun deleteAllRowCounters(knitting: Knitting) {
        context.database.writableDatabase.use { database ->
            val whereClause = RowCounterTable.Cols.KNITTING_ID + "= ?"
            val whereArgs = arrayOf(knitting.id.toString())
            database.delete(RowCounterTable.ROW_COUNTERS, whereClause, whereArgs)
            Log.d(TAG, "Removed knitting ${knitting.id}: $knitting")
        }
    }

    @Synchronized
    fun deleteAllRowCounters() {
        for (r in allRowCounters) {
            deleteRowCounterImpl(r)
        }
        notifyObservers()
    }

    private fun deleteRowCounterImpl(r: RowCounter) {
        context.database.writableDatabase.use { database ->
            database.delete(RowCounterTable.ROW_COUNTERS, RowCounterTable.Cols.ID + "=" + r.id, null)
            Log.d(TAG, "Deleted row counter " + r.id + ": " + r)
        }
        notifyObservers()
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

    fun init(db: AppDatabase) {
        this.db = db
    }
}