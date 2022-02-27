package com.mthaler.knittings.dropbox

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.R
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.service.JobStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class DropboxImportWorker(val database: ExportDatabase<Knitting>, val context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    override fun doWork(): Result {
        TODO("Not yet implemented")
    }

    private fun downloadPhotos(database: ExportDatabase<Knitting>, directory: String) {
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
    private fun getLocalCredential(): DbxCredential? {
        val sharedPreferences = context.getSharedPreferences(KNITTINGS, Context.MODE_PRIVATE)
        val serializedCredential = sharedPreferences.getString("credential", null) ?: return null
        return DbxCredential.Reader.readFully(serializedCredential)
    }

    companion object {
        private  const val TAG = "DropboxImportWorker"

        private const val KNITTINGS = "com.mthaler.knittings"
        const val Database = "com.mthaler.knittings.dropbox.database"
        const val Directory = "com.mthaler.knittings.dropbox.directory"
    }
}