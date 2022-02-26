package com.mthaler.knittings.dropbox

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Knitting

class DropboxImportCreator(val directory: String, val database: ExportDatabase<Knitting>) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return DropboxImportWorker(directory,database, appContext, workerParameters)
    }
}