package com.mthaler.knittings.dropbox

import android.app.PendingIntent
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Project
import com.mthaler.knittings.service.JobStatus

class DropboxImportWorker(val context: Context, parameters: WorkerParameters) : Worker(context, parameters) {


    override fun doWork(): Result {
        TODO("Not yet implemented")
    }

     private fun downloadPhotos(database: ExportDatabase<Project>, directory: String, pendingIntent: PendingIntent) {
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
    }

    //deserialize the credential from SharedPreferences if it exists
    protected fun getLocalCredential(): DbxCredential? {
        val sharedPreferences = context.getSharedPreferences(KNITTINGS, Context.MODE_PRIVATE)
        val serializedCredential = sharedPreferences.getString("credential", null) ?: return null
        return DbxCredential.Reader.readFully(serializedCredential)
    }

    companion object {
        private const val KNITTINGS = "com.mthaler.knittings"
    }
}