package com.mthaler.knittings.dropbox

import android.os.AsyncTask
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.model.Database
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import com.mthaler.knittings.model.*

/**
 * Async task that downloads the database file from Dropbox
 *
 * @arg dbxClient DbxClientV2
 * @arg onDataLoaded callback that is executed when the data is loaded
 * @arg onError callback that is executed if an error happens
 */
internal class DownloadDatabaseTask(private val dbxClient: DbxClientV2,
                                    private val onDataLoaded: (Database?) -> Unit,
                                    private val onError: (Exception) -> Unit) : AsyncTask<String, Void, Database?>() {

    private var exception: Exception? = null

    override fun onPostExecute(result: Database?) {
        super.onPostExecute(result)

        val ex = exception

        if (ex != null) {
            onError(ex)
        } else {
            onDataLoaded(result)
        }
    }

    override fun doInBackground(vararg params: String): Database? {
        // try to download database file from Dropbox and convert it to Database object
        try {
            val os = ByteArrayOutputStream()
            dbxClient.files().download("/" + params[0] + "/db.json").download(os)
            val bytes = os.toByteArray()
            val jsonStr = String(bytes)
            val json = JSONObject(jsonStr)
            val db = json.toDatabase()
            return db
        } catch (e: Exception) {
            exception = e
            return null
        }
    }
}
