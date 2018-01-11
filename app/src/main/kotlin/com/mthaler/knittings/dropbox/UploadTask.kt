package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class UploadTask(private val dbxClient: DbxClientV2, private val file: File, private val context: Context) : AsyncTask<Any, Any?, Any?>() {

    override fun doInBackground(params: Array<Any>): Any? {
        try {
            // Upload to Dropbox
            val inputStream = FileInputStream(file)
            dbxClient.files().uploadBuilder("/" + file.name) //Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) //always overwrite existing file
                    .uploadAndFinish(inputStream)
            Log.d("Upload Status", "Success")
        } catch (e: DbxException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    override fun onPostExecute(o: Any?) {
        super.onPostExecute(o)
        Toast.makeText(context, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
    }
}

