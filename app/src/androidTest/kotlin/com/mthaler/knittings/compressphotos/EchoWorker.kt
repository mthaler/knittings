package com.mthaler.knittings.compressphotos

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class EchoWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {
   override fun doWork(): Result {
       return when(inputData.size()) {
           0 -> Result.failure()
           else -> Result.success(inputData)
       }
   }
}
