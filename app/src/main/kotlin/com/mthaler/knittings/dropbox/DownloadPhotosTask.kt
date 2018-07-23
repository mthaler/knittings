package com.mthaler.knittings.dropbox

import android.os.AsyncTask
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.utils.FileUtils
import java.io.FileOutputStream

class DownloadPhotosTask(private val dbxClient: DbxClientV2,
                         private val directory: String,
                         private val database: Database,
                         private val updateProgress: (Int) -> Unit,
                         private val onComplete: () -> Unit) : AsyncTask<Any, Int?, Any?>() {

    override fun doInBackground(params: Array<Any>): Any? {
        val count = database.photos.size
        for ((index, photo) in database.photos.withIndex()) {
            // Download the file.
            val filename = "/" + directory + "/" + photo.id + "." + FileUtils.getExtension(photo.filename.name)
            FileOutputStream(photo.filename).use {
                dbxClient.files().download(filename).download(it)
            }
            publishProgress((index / count.toFloat() * 100).toInt())

        }
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        updateProgress(values[0]!!)
    }

    override fun onPostExecute(o: Any?) {
        updateProgress(100)
        onComplete()
    }
}