package com.mthaler.knittings.dropbox

import android.content.Context
import androidx.core.net.toUri
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.toDatabase
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.FileUtils
import com.mthaler.knittings.utils.PictureUtils
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class DropboxImportWorker(context: Context, parameters: WorkerParameters) : AbstractDropboxWorker(context, parameters) {

    override suspend  fun doWork(): Result {
        DropboxImportServiceManager.getInstance().updateServiceStatus(ServiceStatus.Started)
        val directory = inputData.getString(Directory)!!
        val database = readDatabase(context, directory, inputData.getString(Database)!!)
        downloadPhotos(database, directory)
        DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Success(context.resources.getString(R.string.dropbox_import_completed)))
        DropboxImportServiceManager.getInstance().updateServiceStatus(ServiceStatus.Stopped)
        return Result.success()
    }

    private fun downloadPhotos(database: Database, directory: String) {
        val sm = DropboxImportServiceManager.getInstance()
        val count = database.photos.size
        val dbxClient = DropboxClientFactory.getClient()
        // remove all existing entries from the database
        KnittingsDataSource.deleteAllProjects()
        KnittingsDataSource.deleteAllPhotos()
        KnittingsDataSource.deleteAllCategories()
        KnittingsDataSource.deleteAllNeedles()
        KnittingsDataSource.deleteAllRowCounters()
        // add downloaded database
        for (photo in database.photos) {
            KnittingsDataSource.addPhoto(photo, manualID = true)
        }
        for (category in database.categories) {
            KnittingsDataSource.addCategory(category, manualID = true)
        }
        for (needle in database.needles) {
            KnittingsDataSource.addNeedle(needle, manualID = true)
        }
        for (r in database.rowCounters) {
            KnittingsDataSource.addRowCounter(r, manualID = true)
        }
        for (knitting in database.projects) {
            KnittingsDataSource.addProject(knitting, manualID = true)
        }
        for ((index, photo) in database.photos.withIndex()) {
            // Download the file.
            val filename = "/" + directory + "/" + photo.id + "." + FileUtils.getExtension(photo.filename.name)
            FileOutputStream(photo.filename).use {
                dbxClient.files().download(filename).download(it)
            }
            // generate preview
            val orientation = PictureUtils.getOrientation(photo.filename.toUri(), context)
            val preview = PictureUtils.decodeSampledBitmapFromPath(photo.filename.absolutePath, 200, 200)
            val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
            val photoWithPreview = photo.copy(preview = rotatedPreview)
            KnittingsDataSource.updatePhoto(photoWithPreview)
            val progress = (index / count.toFloat() * 100).toInt()
            sm.updateJobStatus(JobStatus.Progress(progress))
        }
    }

    companion object {
        val TAG = "com.mthaler.knittings.compressphotos.DropboxImportWorkerr"

        private const val KNITTINGS = "com.mthaler.knittings"
        const val Database = "com.mthaler.knittings.dropbox.database"
        const val Directory = "com.mthaler.knittings.dropbox.directory"

        fun data(directory: String, database: Database): Data {
            val data = Data.Builder()
            data.putString(Directory, directory)
            data.putString(Database, database.toJSON().toString())
            return data.build()
        }

        private fun readDatabase(context: Context, directory: String, database: String): Database {
            val json = JSONObject(database)
            val file = File(directory)
            val db = json.toDatabase(context, file)
            return db
        }
    }
}