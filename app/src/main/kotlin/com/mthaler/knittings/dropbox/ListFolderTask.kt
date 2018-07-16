package com.mthaler.knittings.dropbox

import android.os.AsyncTask

import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult

/**
 * Async task to list items in a folder
 */
internal class ListFolderTask(private val dbxClient: DbxClientV2,
                              private val onDataLoaded: (ListFolderResult) -> Unit,
                              private val onError: (Exception) -> Unit) : AsyncTask<String, Void, ListFolderResult>() {

    private var mException: Exception? = null

    override fun onPostExecute(result: ListFolderResult) {
        super.onPostExecute(result)

        val ex = mException

        if (ex != null) {
            onError(ex)
        } else {
            onDataLoaded(result)
        }
    }

    override fun doInBackground(vararg params: String): ListFolderResult? {
        try {
            return dbxClient.files().listFolder(params[0])
        } catch (e: DbxException) {
            mException = e
        }

        return null
    }
}
