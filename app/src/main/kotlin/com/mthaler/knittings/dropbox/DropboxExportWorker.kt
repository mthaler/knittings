package com.mthaler.knittings.dropbox

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.model.Project
import com.mthaler.knittings.service.JobStatus
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import com.mthaler.knittings.utils.FileUtils.getExtension
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class DropboxExportWorker(val context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    override fun doWork(): Result {
          GlobalScope.launch {
                val dir = createDateTimeDirectoryName(Date())
            val cancelled = withContext(Dispatchers.IO) {
                try {
                    upload(dir)
                } catch (ex: Exception) {
                    Log.e(TAG, "Could not upload: " + ex)
                }
            }
            onUploadCompleted()
        }

        TODO("Not yet implemented")
    }

    private fun upload(dir: String): Boolean {
        val clientIdentifier = "Knittings"
        val requestConfig = DbxRequestConfig(clientIdentifier)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val sm = DropboxExportServiceManager.getInstance()
            dropboxClient.files().createFolderV2("/$dir")
            val database = (applicationContext as DatabaseApplication<Project>).createExportDatabase().checkDatabase()
            uploadDatabase(dropboxClient, dir, database)
            // upload photos to dropbox
            val count = database.photos.size
            for ((index, photo) in database.photos.withIndex()) {
                if (sm.cancelled) {
                    return true
                }
                uploadPhoto(dropboxClient, dir, photo)
                val progress = (index / count.toFloat() * 100).toInt()
                sm.updateJobStatus(JobStatus.Progress(progress))
            }
        }
        return false
    }

    private fun uploadDatabase(dbxClient: DbxClientV2, dir: String, database: ExportDatabase<Project>) {
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


    //deserialize the credential from SharedPreferences if it exists
    private fun getLocalCredential(): DbxCredential? {
        val sharedPreferences = context.getSharedPreferences(DropboxExportService.KNITTINGS, Activity.MODE_PRIVATE)
        val serializedCredential = sharedPreferences.getString("credential", null) ?: return null
        return DbxCredential.Reader.readFully(serializedCredential)
    }

    companion object {
        const val TAG = "DropboxExportWorker"
    }
}