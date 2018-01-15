package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.mthaler.knittings.database.KnittingsDataSource
import java.io.FileInputStream
import java.io.IOException
import com.mthaler.knittings.model.dbToJSON
import java.io.ByteArrayInputStream

class UploadTask(private val dbxClient: DbxClientV2, private val context: Context, private val progressBar: ProgressBar) : AsyncTask<Any, Int?, Any?>() {

    override fun doInBackground(params: Array<Any>): Any? {
        try {
            val ds = KnittingsDataSource.getInstance(context)
            val knittings = ds.allKnittings
            val photos = ds.allPhotos
            val dbJSON = dbToJSON(knittings, photos)
            val s = dbJSON.toString(2)
            val dbInputStream = ByteArrayInputStream(s.toByteArray())
            // upload database to dropbox
            dbxClient.files().uploadBuilder("/" + "db.json") //Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(dbInputStream)
            // upload photos to dropbox
            val count = photos.size
            for ((index, photo) in photos.withIndex()) {
                if (isCancelled) break
                publishProgress((index / count.toFloat() * 100).toInt())
                val file = photo.filename
                val inputStream = FileInputStream(file)
                dbxClient.files().uploadBuilder("/" + file.name) //Path in the user's Dropbox to save the file.
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
        progressBar.setProgress(values[0]!!)
    }

    override fun onPostExecute(o: Any?) {
        super.onPostExecute(o)
        progressBar.setProgress(100)
        Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
    }
}

