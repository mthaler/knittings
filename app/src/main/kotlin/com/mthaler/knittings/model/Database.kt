package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.dbapp.model.Category
import com.mthaler.dbapp.model.ExportDatabase
import com.mthaler.dbapp.model.Photo
import com.mthaler.dbapp.model.categoriesToJSON
import com.mthaler.dbapp.utils.FileUtils
import com.mthaler.dbapp.utils.PictureUtils
import com.mthaler.knittings.database.KnittingDatabaseHelper
import com.mthaler.knittings.database.KnittingsDataSource
import org.json.JSONObject
import java.io.FileOutputStream
import java.io.Serializable
import java.lang.IllegalArgumentException

data class Database(val knittings: List<Knitting>, override val photos: List<Photo>, val categories: List<Category>, val needles: List<Needle>, val rowCounters: List<RowCounter>) : ExportDatabase, Serializable, Parcelable {

    private constructor(parcel: Parcel) : this(
            knittings = parcel.readParcelableArray(classLoader)!!.map { it as Knitting },
            photos = parcel.readParcelableArray(classLoader)!!.map { it as Photo },
            categories = parcel.readParcelableArray(classLoader)!!.map { it as Category },
            needles = parcel.readParcelableArray(classLoader)!!.map { it as Needle },
            rowCounters = parcel.readParcelableArray(classLoader)!!.map { it as RowCounter}
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelableArray(knittings.toTypedArray(), 0)
        parcel.writeParcelableArray(photos.toTypedArray(), 0)
        parcel.writeParcelableArray(categories.toTypedArray(), 0)
        parcel.writeParcelableArray(needles.toTypedArray(), 0)
        parcel.writeParcelableArray(rowCounters.toTypedArray(), 0)
    }

    override fun describeContents(): Int = 0

    override fun checkValidity() {
        checkPhotosValidity()
        checkKnittingsValidity()
    }

    private fun checkPhotosValidity() {
        val missing =  photos.map {it.ownerID}.toSet() - knittings.map { it.id }.toSet()
        if (missing.isNotEmpty()) {
            throw IllegalArgumentException("Photos reference non-existing knittings with ids $missing")
        }
    }

    private fun checkKnittingsValidity() {
        val missingCategories = knittings.mapNotNull { it.category }.map { it.id }.toSet() - categories.map { it.id }.toSet()
        if (missingCategories.isNotEmpty()) {
            throw IllegalArgumentException("Knittings reference non-existing categories with ids $missingCategories")
        }
        val missingPhotos = knittings.mapNotNull { it.defaultPhoto }.map { it.id }.toSet() - photos.map { it.id }.toSet()
        if (missingPhotos.isNotEmpty()) {
            throw IllegalArgumentException("Knittings reference non-existing photos with ids $missingPhotos")
        }
    }


    override fun checkDatabase(): Database {
        val filteredPhotos = photos.filter { it.filename.exists() }
        val removedPhotos = photos.map { it.id }.toSet() - filteredPhotos.map { it.id}.toSet()
        val updatedKnittings = knittings.map { if (removedPhotos.contains(it.defaultPhoto?.id)) it.copy(defaultPhoto = null) else it }
        val filteredDatabase = copy(knittings = updatedKnittings, photos = filteredPhotos)
        filteredDatabase.checkValidity()
        return filteredDatabase
    }

    override fun removeMissingPhotos(missingPhotos: Set<Long>): ExportDatabase {
        val filteredPhotos = photos.filterNot { missingPhotos.contains(it.id) }
        val updatedKnittings = knittings.map { if (missingPhotos.contains(it.defaultPhoto?.id)) it.copy(defaultPhoto = null) else it }
        val filteredDatabase = copy(knittings = updatedKnittings, photos = filteredPhotos)
        filteredDatabase.checkValidity()
        return filteredDatabase
    }

    override fun write(dbxClient: DbxClientV2, directory: String, photoDownloaded: (Int) -> Unit) {
        val count = photos.size
        // remove all existing entries from the database
        KnittingsDataSource.deleteAllProjects()
        KnittingsDataSource.deleteAllPhotos()
        KnittingsDataSource.deleteAllCategories()
        KnittingsDataSource.deleteAllNeedles()
        KnittingsDataSource.deleteAllRowCounters()
        // add downloaded database
        for (photo in photos) {
            KnittingsDataSource.addPhoto(photo, manualID = true)
        }
        for (category in categories) {
            KnittingsDataSource.addCategory(category, manualID = true)
        }
        for (needle in needles) {
            KnittingsDataSource.addNeedle(needle, manualID = true)
        }
        for (r in rowCounters) {
            KnittingsDataSource.addRowCounter(r, manualID = true)
        }
        for (knitting in knittings) {
            KnittingsDataSource.addProject(knitting, manualID = true)
        }
        for ((index, photo) in photos.withIndex()) {
            val filename = "/" + directory + "/" + photo.id + "." + FileUtils.getExtension(photo.filename.name)
            FileOutputStream(photo.filename).use {
                dbxClient.files().download(filename).download(it)
            }
            // generate preview
            val orientation = PictureUtils.getOrientation(photo.filename.absolutePath)
            val preview = PictureUtils.decodeSampledBitmapFromPath(photo.filename.absolutePath, 200, 200)
            val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
            val photoWithPreview = photo.copy(preview = rotatedPreview)
            KnittingsDataSource.updatePhoto(photoWithPreview)
            val progress = (index / count.toFloat() * 100).toInt()
            photoDownloaded(progress)
        }
    }

    override fun toJSON(): JSONObject {
        val result = JSONObject()
        result.put("version", KnittingDatabaseHelper.DB_VERSION)
        result.put("knittings", knittingsToJSON(knittings))
        result.put("photos", photosToJSON(photos))
        result.put("categories", categoriesToJSON(categories))
        result.put("needles", needlesToJSON(needles))
        result.put("rowCounters", rowCountersToJSON(rowCounters))
        return result
    }

    companion object {

        val classLoader: ClassLoader = javaClass.classLoader!!

        @JvmField
        val CREATOR = object : Parcelable.Creator<Database> {
            override fun createFromParcel(parcel: Parcel) = Database(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Database>(size)
        }

        fun createDatabase(): Database {
            val knittings = KnittingsDataSource.allProjects
            val photos = KnittingsDataSource.allPhotos
            val categories = KnittingsDataSource.allCategories
            val needles = KnittingsDataSource.allNeedles
            val rowCounters = KnittingsDataSource.allRowCounters
            return Database(knittings, photos, categories, needles, rowCounters)
        }
    }
}