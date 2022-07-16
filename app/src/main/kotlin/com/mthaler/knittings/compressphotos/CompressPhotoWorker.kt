package com.mthaler.knittings.compressphotos

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.utils.PictureUtils
import com.mthaler.knittings.utils.WorkerUtils
import com.mthaler.knittings.utils.copy
import java.lang.Exception

class CompressPhotoWorker(val context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        try {
            val sm = CompressPhotosServiceManager.getInstance()
            val cancelled = compressPhotos()
            if (cancelled) {
                sm.updateJobStatus(JobStatus.Cancelled(context.getString(R.string.compress_photos_cancelled)))
                return Result.failure()
            } else {
                sm.updateJobStatus(JobStatus.Success(context.getString(R.string.compress_photos_completed)))
                return Result.success()
            }
        } catch (ex: Exception) {
            return Result.failure()
        }
    }

    private suspend fun compressPhotos(): Boolean  {
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
                compressed.copy(file)
                if (!compressed.delete()) {
                    error("Could not delete $compressed")
                }
            } else {
                if (!compressed.delete()) {
                    error("Could not delete $compressed")
                }
            }
            setProgressAsync(Data.Builder().putInt(WorkerUtils.Progress, progress).build())
            sm.updateJobStatus(JobStatus.Progress(progress))
        }
        return false
    }
}