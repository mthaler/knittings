package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.mthaler.knittings.database.KnittingsDataSource
import java.io.FileInputStream
import java.io.IOException
import com.mthaler.knittings.model.dbToJSON
import java.io.ByteArrayInputStream
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName
import java.util.*

class UploadTask(private val dbxClient: DbxClientV2,
                 private val context: Context,
                 private val updateProgress: (Int) -> Unit,
                 private val setMode: (Boolean) -> Unit) : AsyncTask<Any, Int?, Any?>() {

    override fun doInBackground(params: Array<Any>): Any? {
        try {
            val ds = KnittingsDataSource.getInstance(context)
            val knittings = ds.allKnittings
            val photos = ds.allPhotos
            val dbJSON = dbToJSON(knittings, photos)
            val s = dbJSON.toString(2)
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
                publishProgress((index / count.toFloat() * 100).toInt())
                val file = photo.filename
                val inputStream = FileInputStream(file)
                dbxClient.files().uploadBuilder("/" + dir + "/" + file.name) //Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(inputStream)
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
        setMode(false)
        Toast.makeText(context, "Export finished", Toast.LENGTH_SHORT).show()
    }

    override fun onCancelled() {
        super.onCancelled()
        setMode(false)
    }
}

