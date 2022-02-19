package com.mthaler.knittings.compressphotos

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.utils.FileUtils
import com.mthaler.knittings.utils.PictureUtils
import java.lang.Exception

class CompressPhotoWorker(val context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        try {
            compressPhotos()
            return Result.success()
        } catch (ex: Exception) {
            return Result.failure()
        }
    }

    private suspend fun compressPhotos() {
        val sm = CompressPhotosServiceManager.getInstance()
        val photos = KnittingsDataSource.allPhotos
        val photosToCompress = photos.filter {
            it.filename.exists() && it.filename.length() > 350 * 1024
        }
        val count = photosToCompress.count()
        for ((index, photo) in photosToCompress.withIndex()) {
            if (sm.cancelled) {
                return
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
            setProgressAsync(Data.Builder().putInt(Progress, progress).build())
            sm.updateJobStatus(JobStatus.Progress(progress))
        }
    }

    companion object {
        const val Progress = "Progress"
    }
}