package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.AsyncTask
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.utils.PictureUtils
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.utils.FileUtils
import java.io.FileOutputStream

/**
 * Async task that downloads all photos from Dropbox and stores them locally. It also generates the preview and stores it in the database
 *
 * @param dbxClient Dropbox client
 * @param context context
 * @param directory Dropbox backup directory
 * @param database database
 * @param updateProgress function to update progess
 * @param onComplete function that is called when task is completed
 */
class DownloadPhotosTask(private val dbxClient: DbxClientV2,
                         private val context: Context,
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
            // generate preview
            val orientation = PictureUtils.getOrientation(photo.filename.absolutePath)
            val preview = PictureUtils.decodeSampledBitmapFromPath(photo.filename.absolutePath, 200, 200)
            val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
            val photoWithPreview = photo.copy(preview = rotatedPreview)
            context.datasource.updatePhoto(photoWithPreview)
            // update progress
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