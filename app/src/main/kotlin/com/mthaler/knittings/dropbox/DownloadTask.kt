package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.AsyncTask
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.utils.FileUtils
import java.io.ByteArrayOutputStream

class DownloadTask(private val dbxClient: DbxClientV2,
                   private val context: Context,
                   private val database: Database) : AsyncTask<Any, Int?, Any?>() {

    override fun doInBackground(params: Array<Any>): Any? {
        for (photo in database.photos) {
            // Download the file.
            val out = ByteArrayOutputStream()
            val filename = "" + photo.id + FileUtils.getExtension(photo.filename.name)
            dbxClient.files().download(filename).download(out)
            val bytes = out.toByteArray()
            println("Downloaded " + bytes.size)
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {

    }

    override fun onPostExecute(o: Any?) {

    }

    override fun onCancelled() {

    }
}