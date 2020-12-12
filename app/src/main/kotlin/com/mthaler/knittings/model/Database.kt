package com.mthaler.knittings.model

import android.os.Parcel
import android.os.Parcelable
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.dbapp.model.*
import com.mthaler.dbapp.utils.FileUtils
import com.mthaler.dbapp.utils.PictureUtils
import com.mthaler.knittings.database.KnittingDatabaseHelper
import com.mthaler.knittings.database.KnittingsDataSource
import org.json.JSONObject
import java.io.FileOutputStream

data class Database(override val projects: List<Knitting>, override val photos: List<Photo>, override val categories: List<Category>, val needles: List<Needle>, val rowCounters: List<RowCounter>) : AbstractExportDatabase<Knitting>() {

    private constructor(parcel: Parcel) : this(
            projects = parcel.readParcelableArray(classLoader)!!.map { it as Knitting },
            photos = parcel.readParcelableArray(classLoader)!!.map { it as Photo },
            categories = parcel.readParcelableArray(classLoader)!!.map { it as Category },
            needles = parcel.readParcelableArray(classLoader)!!.map { it as Needle },
            rowCounters = parcel.readParcelableArray(classLoader)!!.map { it as RowCounter}
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelableArray(projects.toTypedArray(), 0)
        parcel.writeParcelableArray(photos.toTypedArray(), 0)
        parcel.writeParcelableArray(categories.toTypedArray(), 0)
        parcel.writeParcelableArray(needles.toTypedArray(), 0)
        parcel.writeParcelableArray(rowCounters.toTypedArray(), 0)
    }

    override fun describeContents(): Int = 0

    override fun checkDatabase(): Database {
        val filteredPhotos = photos.filter { it.filename.exists() }
        val removedPhotos = photos.map { it.id }.toSet() - filteredPhotos.map { it.id}.toSet()
        val updatedKnittings = projects.map { if (removedPhotos.contains(it.defaultPhoto?.id)) it.copy(defaultPhoto = null) else it }
        val filteredDatabase = copy(projects = updatedKnittings, photos = filteredPhotos)
        filteredDatabase.checkValidity()
        return filteredDatabase
    }

    override fun removeMissingPhotos(missingPhotos: Set<Long>): ExportDatabase<Knitting> {
        val filteredPhotos = photos.filterNot { missingPhotos.contains(it.id) }
        val updatedKnittings = projects.map { if (missingPhotos.contains(it.defaultPhoto?.id)) it.copy(defaultPhoto = null) else it }
        val filteredDatabase = copy(projects = updatedKnittings, photos = filteredPhotos)
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
        for (knitting in projects) {
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
        result.put("knittings", knittingsToJSON(projects))
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
            val projects = KnittingsDataSource.allProjects
            val photos = KnittingsDataSource.allPhotos
            val categories = KnittingsDataSource.allCategories
            val needles = KnittingsDataSource.allNeedles
            val rowCounters = KnittingsDataSource.allRowCounters
            return Database(projects, photos, categories, needles, rowCounters)
        }
    }
}