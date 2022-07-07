package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Data
import androidx.work.WorkerParameters
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.model.toDatabase
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.FileUtils
import com.mthaler.knittings.utils.PictureUtils
import com.mthaler.knittings.utils.getOrientation
import com.mthaler.knittings.utils.removeLeadingChars
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.IllegalArgumentException

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
        val requestConfig = DbxRequestConfig(CLIENT_IDENTIFIER)
        val credential = getLocalCredential()
        val sm = DropboxImportServiceManager.getInstance()
        val count = database.photos.size
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            // remove all existing entries from the database
            try {
                KnittingsDataSource.deleteAllProjects()
                KnittingsDataSource.deleteAllPhotos()
                KnittingsDataSource.deleteAllCategories()
                KnittingsDataSource.deleteAllNeedles()
                KnittingsDataSource.deleteAllRowCounters()
            } catch (ex: FileNotFoundException) {
                Log.e(TAG, "Could not delete file", ex)
            }
            // add downloaded database
            for (photo in database.photos) {
                val filename = if (photo.filename.name.startsWith("/")) File(photo.filename.name.substring(1, photo.filename.name.length)).name else photo.filename.name
                val p = photo.copy(filename = File(filename))
                KnittingsDataSource.addPhoto(p, manualID = true)
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
            for (knitting in database.knittings) {
                KnittingsDataSource.addProject(knitting, manualID = true)
            }
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            Log.d(TAG, "storage dir: " + storageDir)
            if (storageDir != null) {
                for ((index, photo) in database.photos.withIndex()) {
                    downloadPhoto(directory, dropboxClient, photo, index, sm, count, storageDir)
                    generatePreview(photo, storageDir)
                }
            } else {
                throw IllegalArgumentException("Storage dir null")
            }
        }
    }

    private fun downloadPhoto(directory: String, dropboxClient: DbxClientV2, photo: Photo, index: Int, sm: DropboxImportServiceManager, count: Int, storageDir: File) {
        try {
            // Download the file.
            val dropboxFilename = "/" + directory + "/" + photo.id + "." + FileUtils.getExtension("" + photo.filename)
            Log.d(TAG,"Downloading file to $photo")
            val localPath = File(storageDir, photo.filename.name.removeLeadingChars('/'))
            FileOutputStream(localPath).use {
                dropboxClient.files().download(dropboxFilename).download(it)
                Log.d(TAG, "Downloaded photo $photo")
            }
        } finally {
            val progress = (index / count.toFloat() * 100).toInt()
            sm.updateJobStatus(JobStatus.Progress(progress))
        }
    }


    private fun generatePreview(photo: Photo, storageDir: File) {
        val f = File(storageDir, photo.filename.name)
        Log.d(TAG, "generating preview for $f")
        val orientation = f.toUri().getOrientation(context)
        val preview = PictureUtils.decodeSampledBitmapFromPath(f.absolutePath, 200, 200)
        val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
        val photoWithPreview = photo.copy(preview = rotatedPreview)
        KnittingsDataSource.updatePhoto(photoWithPreview)
        Log.d(TAG, "gemerated preview for $photo")
    }

    companion object {
        val TAG = "DropboxImportWorker"

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