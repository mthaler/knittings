package com.mthaler.knittings.model

import android.content.Context
import android.net.Uri
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.utils.FileUtils
import com.mthaler.knittings.database.KnittingDatabaseHelper
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.utils.PictureUtils
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class Database(override val projects: List<Knitting>, override val photos: List<Photo>, override val categories: List<Category>, val needles: List<Needle>, val rowCounters: List<RowCounter>) : AbstractExportDatabase<Knitting>() {
 
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

    override fun write(context: Context, dbxClient: DbxClientV2, directory: String, photoDownloaded: (Int) -> Unit) {
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
            val orientation = PictureUtils.getOrientation(Uri.fromFile(File(photo.filename.absolutePath)), context)
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