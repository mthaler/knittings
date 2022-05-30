package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.PowerManager
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.R
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import org.json.JSONObject
import java.io.File

class DropboxImportWorker(val context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    override suspend  fun doWork(): Result {
        DropboxImportServiceManager.getInstance().updateServiceStatus(ServiceStatus.Started)
        val directory = inputData.getString(Directory)!!
        val app: DatabaseApplication = context.applicationContext as DatabaseApplication
        val database = readDatabase(app, directory, inputData.getString(Database)!!)
        val wakeLock: PowerManager.WakeLock =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Knittings::DropboxImport").apply {
                acquire()
            }
        }
        try {
            downloadPhotos(database, directory)
        } finally {
            wakeLock.release()
        }
        DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Success(context.resources.getString(R.string.dropbox_import_completed)))
        return Result.success()
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
        val TAG = "com.mthaler.knittings.compressphotos.DropboxImportWorkerr"

        private const val KNITTINGS = "com.mthaler.knittings"
        const val Database = "com.mthaler.knittings.dropbox.database"
        const val Directory = "com.mthaler.knittings.dropbox.directory"

        fun data(directory: String, database: ExportDatabase<Knitting>): Data {
            val data = Data.Builder()
            data.putString(Directory, directory)
            data.putString(Database, database.toJSON().toString())
            return data.build()
        }

        fun readDatabase(application: DatabaseApplication, directory: String, database: String): ExportDatabase<Knitting> {
            val json = JSONObject(database)
            val file = File(directory)
            val db = application.createExportDatabaseFromJSON(json, file)
            return db
        }
    }
}