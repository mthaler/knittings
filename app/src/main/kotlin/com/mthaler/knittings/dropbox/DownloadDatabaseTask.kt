package com.mthaler.knittings.dropbox

import android.os.AsyncTask
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 */
internal class DownloadDatabaseTask(private val dbxClient: DbxClientV2,
                                    private val onDataLoaded: (JSONObject) -> Unit,
                                    private val onError: (Exception) -> Unit) : AsyncTask<String, Void, JSONObject>() {

    private var mException: Exception? = null

    override fun onPostExecute(result: JSONObject) {
        super.onPostExecute(result)

        val ex = mException

        if (ex != null) {
            onError(ex)
        } else {
            onDataLoaded(result)
        }
    }

    override fun doInBackground(vararg params: String): JSONObject? {
        try {
            val os = ByteArrayOutputStream()
            dbxClient.files().download("/" + params[0] + "/db.json").download(os)
            val bytes = os.toByteArray()
            val jsonStr = String(bytes)
            return JSONObject(jsonStr)
        } catch (e: DbxException) {
            mException = e
        } catch (e: IOException) {
            mException = e
        }

        return null
    }
}
