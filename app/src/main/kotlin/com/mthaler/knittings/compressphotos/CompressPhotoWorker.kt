package com.mthaler.knittings.compressphotos

import android.app.PendingIntent
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.utils.FileUtils
import com.mthaler.knittings.utils.PictureUtils

class CompressPhotoWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        TODO("Not yet implemented")
    }

    private suspend fun compressPhotos(pendingIntent: PendingIntent): Boolean {
        val sm = CompressPhotosServiceManager.getInstance()
        val photos = KnittingsDataSource.allPhotos
        val photosToCompress = photos.filter {
            it.filename.exists() && it.filename.length() > 350 * 1024
        }
        val count = photosToCompress.count()
        for ((index, photo) in photosToCompress.withIndex()) {
            if (sm.cancelled) {
                return true
            }
            val progress = (index / count.toDouble() * 100).toInt()
            val file = photo.filename
            val compressed = PictureUtils.compress(context, file)
            if (compressed.length() < file.length()) {
                if (!file.delete()) {
                    error("Could not delete $file")
                }
                FileUtils.copy(compressed, file)
                if (!compressed.delete()) {
                    error("Could not delete $compressed")
                }
            } else {
                if (!compressed.delete()) {
                    error("Could not delete $compressed")
                }
            }
            sm.updateJobStatus(JobStatus.Progress(progress))
        }
        return false
    }
}