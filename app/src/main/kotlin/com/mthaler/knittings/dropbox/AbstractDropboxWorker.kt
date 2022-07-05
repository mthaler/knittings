package com.mthaler.knittings.dropbox

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dropbox.core.oauth.DbxCredential

abstract class AbstractDropboxWorker(protected  val context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    //deserialize the credential from SharedPreferences if it exists
    protected fun getLocalCredential(): DbxCredential? {
        val sharedPreferences = context.getSharedPreferences(AbstractDropboxFragment.KNITTINGS, Activity.MODE_PRIVATE)
        val serializedCredential = sharedPreferences.getString("credential", null) ?: return null
        return DbxCredential.Reader.readFully(serializedCredential)
    }

    //serialize the credential and store in SharedPreferences
    protected fun storeCredentialLocally(dbxCredential: DbxCredential) {
        val sharedPreferences = context.getSharedPreferences("dropbox-sample", MODE_PRIVATE)
        sharedPreferences.edit().putString("credential", dbxCredential.toString()).apply()
    }
}