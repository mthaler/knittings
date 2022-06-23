package com.mthaler.knittings.dropbox

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.FileUtils
import com.mthaler.knittings.utils.PictureUtils
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class DropboxImportWorker(val context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    override suspend  fun doWork(): Result {
        DropboxImportServiceManager.getInstance().updateServiceStatus(ServiceStatus.Started)
        val directory = inputData.getString(Directory)!!
        val app: DatabaseApplication = context.applicationContext as DatabaseApplication
        val database = readDatabase(app, directory, inputData.getString(Database)!!)
        downloadPhotos(database, directory)
        DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Success(context.resources.getString(R.string.dropbox_import_completed)))
        DropboxImportServiceManager.getInstance().updateServiceStatus(ServiceStatus.Stopped)
        return Result.success()
    }

    private fun downloadPhotos(database: ExportDatabase, directory: String) {
        val clientIdentifier = "Knittings"
        val requestConfig = DbxRequestConfig(clientIdentifier)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val sm = DropboxImportServiceManager.getInstance()
            database.write(context, dropboxClient, directory) { progress ->
                sm.updateJobStatus(JobStatus.Progress(progress))
            }
        }

        val sm = DropboxImportServiceManager.getInstance()
        val count = database.photos.size
        // remove all existing entries from the database
        KnittingsDataSource.deleteAllProjects()
        KnittingsDataSource.deleteAllPhotos()
        KnittingsDataSource.deleteAllCategories()
        KnittingsDataSource.deleteAllNeedles()
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
            val orientation = PictureUtils.getOrientation(photo.filename.absolutePath)
            val preview = PictureUtils.decodeSampledBitmapFromPath(photo.filename.absolutePath, 200, 200)
            val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
            val photoWithPreview = photo.copy(preview = rotatedPreview)
            KnittingsDataSource.updatePhoto(photoWithPreview)
            val progress = (index / count.toFloat() * 100).toInt()
            sm.updateJobStatus(JobStatus.Progress(progress))
        }
    }

    //deserialize the credential from SharedPreferences if it exists
    private fun getLocalCredential(): DbxCredential? {
        val sharedPreferences = context.getSharedPreferences(KNITTINGS, Context.MODE_PRIVATE)
        val serializedCredential = sharedPreferences.getString("credential", null) ?: return null
        return DbxCredential.Reader.readFully(serializedCredential)
    }


    companion object {
        val TAG = "com.mthaler.knittings.compressphotos.DropboxImportWorkerr"

        private const val KNITTINGS = "com.mthaler.knittings"
        const val Database = "com.mthaler.knittings.dropbox.database"
        const val Directory = "com.mthaler.knittings.dropbox.directory"

        fun data(directory: String, database: ExportDatabase): Data {
            val data = Data.Builder()
            data.putString(Directory, directory)
            data.putString(Database, database.toJSON().toString())
            return data.build()
        }

        fun readDatabase(application: DatabaseApplication, directory: String, database: String): ExportDatabase {
            val json = JSONObject(database)
            val file = File(directory)
            val db = application.createExportDatabaseFromJSON(json, file)
            return db
        }
    }
}