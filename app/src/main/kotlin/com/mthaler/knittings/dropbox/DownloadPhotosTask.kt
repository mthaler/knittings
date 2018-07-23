package com.mthaler.knittings.dropbox

import android.os.AsyncTask
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.utils.FileUtils
import java.io.FileOutputStream

class DownloadPhotosTask(private val dbxClient: DbxClientV2,
                         private val directory: String,
                         private val database: Database,
                         private val onComplete: () -> Unit) : AsyncTask<Any, Int?, Any?>() {

    override fun doInBackground(params: Array<Any>): Any? {
        for (photo in database.photos) {
            // Download the file.
            val filename = "/" + directory + "/" + photo.id + "." + FileUtils.getExtension(photo.filename.name)
            FileOutputStream(photo.filename).use {
                dbxClient.files().download(filename).download(it)
            }

        }
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {

    }

    override fun onPostExecute(o: Any?) {
        onComplete()
    }
}