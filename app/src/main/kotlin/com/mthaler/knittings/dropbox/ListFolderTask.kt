package com.mthaler.knittings.dropbox

import android.os.AsyncTask
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult

/**
 * Async task to list items in a folder
 *
 * @arg dbxClient DbxClientV2
 * @arg onDataLoaded callback that is executed when the data is loaded
 * @arg onError callback that is executed if an error happens
 */
internal class ListFolderTask(private val dbxClient: DbxClientV2,
                              private val onDataLoaded: (ListFolderResult) -> Unit,
                              private val onError: (Exception) -> Unit) : AsyncTask<String, Void, ListFolderResult>() {

    private var exception: Exception? = null

    override fun onPostExecute(result: ListFolderResult) {
        super.onPostExecute(result)

        val ex = exception

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
            exception = e
            return null
        }
    }
}
