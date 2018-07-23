package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.mthaler.knittings.database.KnittingsDataSource
import java.io.FileInputStream
import java.io.IOException
import com.mthaler.knittings.model.*
import java.io.ByteArrayInputStream
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName
import com.mthaler.knittings.utils.FileUtils.getExtension
import java.util.*

/**
 * Tasks that uploads the application data to Dropbox
 *
 * @param dbxClient Dropbox client
 * @param context context
 * @param updateProgress function that is called when progress is updates
 * @param onComplete function that is called when upload is completed
 */
class UploadTask(private val dbxClient: DbxClientV2,
                 private val context: Context,
                 private val updateProgress: (Int) -> Unit,
                 private val onComplete: (Boolean) -> Unit) : AsyncTask<Void, Int?, Any?>() {

    override fun doInBackground(vararg params: Void): Any? {
        try {
            val ds = KnittingsDataSource.getInstance(context)
            // get all knittings
            val knittings = ds.allKnittings
            // get all photos
            val photos = ds.allPhotos
            // convert database to JSON
            val dbJSON = Database(knittings, photos).toJSON()
            val s = dbJSON.toString(2)

            // create input stream from database JSON
            val dbInputStream = ByteArrayInputStream(s.toByteArray())

            // create directory containing current date & time
            val dir = createDateTimeDirectoryName(Date())
            dbxClient.files().createFolderV2("/$dir")

            // upload database to dropbox
            dbxClient.files().uploadBuilder("/$dir/db.json") //Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(dbInputStream)

            // upload photos to dropbox
            val count = photos.size
            for ((index, photo) in photos.withIndex()) {
                if (isCancelled) break
                val inputStream = FileInputStream(photo.filename)
                dbxClient.files().uploadBuilder("/" + dir + "/" + photo.id + "." + getExtension(photo.filename.name)) //Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(inputStream)
                publishProgress((index / count.toFloat() * 100).toInt())
            }
            Log.d("Upload Status", "Success")
        } catch (e: DbxException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        updateProgress(values[0]!!)
    }

    override fun onPostExecute(o: Any?) {
        super.onPostExecute(o)
        updateProgress(100)
        onComplete(false)
    }

    override fun onCancelled() {
        super.onCancelled()
        onComplete(true)
    }
}

