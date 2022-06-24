package com.mthaler.knittings.dropbox

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

abstract class AbstractDropboxWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
}