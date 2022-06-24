package com.mthaler.knittings.dropbox

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dropbox.core.oauth.DbxCredential

abstract class AbstractDropboxWorker(val context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    //serialize the credential and store in SharedPreferences
    protected fun storeCredentialLocally(dbxCredential: DbxCredential) {
        val sharedPreferences = context.getSharedPreferences("dropbox-sample", MODE_PRIVATE)
        sharedPreferences.edit().putString("credential", dbxCredential.toString()).apply()
    }
}