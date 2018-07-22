package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.AsyncTask
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.model.Database

class DownloadTask(private val dbxClient: DbxClientV2,
                   private val context: Context,
                   private val database: Database) : AsyncTask<Any, Int?, Any?>() {

    override fun doInBackground(params: Array<Any>): Any? {
        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {

    }

    override fun onPostExecute(o: Any?) {

    }

    override fun onCancelled() {

    }
}