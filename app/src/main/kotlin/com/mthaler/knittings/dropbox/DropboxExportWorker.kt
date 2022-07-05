package com.mthaler.knittings.dropbox

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.service.JobStatus
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import com.mthaler.knittings.utils.FileUtils.getExtension
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class DropboxExportWorker(context: Context, parameters: WorkerParameters) : AbstractDropboxWorker(context, parameters) {

    override  suspend fun doWork(): Result {
        val dir = createDateTimeDirectoryName(Date())
        withContext(Dispatchers.IO) {
            try {
                upload(dir)
            } catch (ex: Exception) {
                Log.e(TAG, "Could not upload: " + ex)
            }
        }
        onUploadCompleted()
        return Result.success()
    }

    private fun upload(dir: String): Boolean {
        val clientIdentifier = "Knittings"
        val requestConfig = DbxRequestConfig(clientIdentifier)
        val credential = getLocalCredential()
        credential?.let {
//            val dropboxClient = DbxClientV2(requestConfig, credential)
//            val sm = DropboxExportServiceManager.getInstance()
//            dropboxClient.files().createFolderV2("/$dir")
//            val database = (applicationContext as DatabaseApplication).createExportDatabase().checkDatabase()
//            uploadDatabase(dropboxClient, dir, database)
//            // upload photos to dropbox
//            val count = database.photos.size
//            for ((index, photo) in database.photos.withIndex()) {
//                if (sm.cancelled) {
//                    return true
//                }
//                uploadPhoto(dropboxClient, dir, photo)
//                val progress = (index / count.toFloat() * 100).toInt()
//                sm.updateJobStatus(JobStatus.Progress(progress))
//            }
        }
        return false
    }

    private fun uploadDatabase(dbxClient: DbxClientV2, dir: String, database: ExportDatabase) {
        val dbJSON = database.toJSON()
        val s = dbJSON.toString(2)
        val dbInputStream = ByteArrayInputStream(s.toByteArray())
        // upload database to dropbox
        dbxClient.files().uploadBuilder("/$dir/db.json") // Path in the user's Dropbox to save the file.
                .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                .uploadAndFinish(dbInputStream)
    }

    private fun uploadPhoto(dbxClient: DbxClientV2, dir: String, photo: Photo) {
        val inputStream = FileInputStream(photo.filename)
        dbxClient.files().uploadBuilder("/" + dir + "/" + photo.id + "." + getExtension(photo.filename.name)) // Path in the user's Dropbox to save the file.
                .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                .uploadAndFinish(inputStream)
    }

    private fun onUploadCompleted() {
        DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Success())
    }

    companion object {
        val TAG = "DropboxExportWorker"

        private const val KNITTINGS = "com.mthaler.knittings"
    }
}