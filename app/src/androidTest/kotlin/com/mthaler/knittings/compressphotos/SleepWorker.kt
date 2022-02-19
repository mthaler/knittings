package com.mthaler.knittings.compressphotos

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class SleepWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    override fun doWork(): Result {
        // Sleep on a background thread.
        Thread.sleep(1000)
        return Result.success()
    }
}
