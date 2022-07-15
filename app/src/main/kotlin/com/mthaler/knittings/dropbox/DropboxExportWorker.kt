package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.Environment
import android.os.PowerManager
import android.util.Log
import androidx.work.WorkerParameters
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.service.JobStatus
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import com.mthaler.knittings.utils.FileUtils.getExtension
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName
import com.mthaler.knittings.utils.removeLeadingChars
import kotlinx.coroutines.*
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class DropboxExportWorker(context: Context, parameters: WorkerParameters) : AbstractDropboxWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val dir = createDateTimeDirectoryName(Date())
        val deferred = GlobalScope.async {
            val wakeLock: PowerManager.WakeLock =
                (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Knittings::DropboxExport").apply {
                        acquire()
                    }
                }
            try {
                upload(dir)
            } finally {
                wakeLock.release()
            }
        }

        val cancelled = deferred.await()
        onUploadCompleted(dir, cancelled)
        return Result.success()
    }

    private fun upload(dir: String): Boolean {
        val requestConfig = DbxRequestConfig(CLIENT_IDENTIFIER)
        val credential = getLocalCredential()
        val sm = DropboxExportServiceManager.getInstance()
        // create directory containing current date & time
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            dropboxClient.files().createFolderV2("/$dir")
            val database = checkDatabase(createDatabase())
            uploadDatabase(dropboxClient, dir, database)
            // upload photos to dropbox
            val count = database.photos.size
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            Log.d(TAG, "storage dir: " + storageDir)
            if (storageDir != null) {
                for ((index, photo) in database.photos.withIndex()) {
                    if (sm.cancelled) {
                        return true
                    }
                    uploadPhoto(dropboxClient, dir, photo, storageDir)
                    val progress = (index / count.toFloat() * 100).toInt()
                    sm.updateJobStatus(JobStatus.Progress(progress))
                }
            } else {
                throw IllegalArgumentException("Storage dir null")
            }
        }
        return false
    }

    private fun createDatabase(): Database {
        val knittings = KnittingsDataSource.allProjects
        val photos = KnittingsDataSource.allPhotos
        val categories = KnittingsDataSource.allCategories
        val needles = KnittingsDataSource.allNeedles
        val rowCounters = KnittingsDataSource.allRowCounters
        return Database(knittings, photos, categories, needles, rowCounters)
    }

    private fun checkDatabase(database: Database): Database {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        Log.d(TAG, "storage dir: " + storageDir)
        if (storageDir != null) {
            val filteredPhotos = database.photos.filter { File(storageDir, it.filename.name).exists() }
            val removedPhotos = database.photos.map { it.id }.toSet() - filteredPhotos.map { it.id}.toSet()
            val updatedKnittings = database.knittings.map { if (removedPhotos.contains(it.defaultPhoto?.id)) it.copy(defaultPhoto = null) else it }
            val filteredDatabase = database.copy(knittings = updatedKnittings, photos = filteredPhotos)
            filteredDatabase.checkValidity()
            return filteredDatabase
        } else {
            throw IllegalArgumentException("Storage dir null")
        }
    }

    private fun uploadDatabase(dbxClient: DbxClientV2, dir: String, database: Database) {
        val dbJSON = database.toJSON()
        val s = dbJSON.toString(2)
        val dbInputStream = ByteArrayInputStream(s.toByteArray())
        // upload database to dropbox
        dbxClient.files().uploadBuilder("/$dir/db.json") // Path in the user's Dropbox to save the file.
                .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                .uploadAndFinish(dbInputStream)
    }

    private fun uploadPhoto(dbxClient: DbxClientV2, dir: String, photo: Photo, storageDir: File) {
        val localPath = File(storageDir, photo.filename.name.removeLeadingChars('/'))
        FileInputStream(localPath).use {
            dbxClient.files().uploadBuilder("/" + dir + "/" + photo.id + "." + getExtension(photo.filename.name)) // Path in the user's Dropbox to save the file.
                .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                .uploadAndFinish(it)
        }
        Log.d(TAG, "Uploaded photo $photo")
    }


    private fun onUploadCompleted(dir: String, cancelled: Boolean) {
        if (cancelled) {
            DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Cancelled(context.getString(R.string.dropbox_export_notification_cancelled_msg), dir))
        } else {
            DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Success())
        }
    }


    companion object {
        val TAG = "DropboxExportWorker"
    }
}