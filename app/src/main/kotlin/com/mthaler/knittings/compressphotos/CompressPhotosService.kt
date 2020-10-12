package com.mthaler.knittings.compressphotos

import android.app.*
import android.content.Intent
import android.content.Context
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mthaler.dbapp.utils.FileUtils
import com.mthaler.dbapp.utils.PictureUtils
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.dbapp.service.JobStatus
import com.mthaler.dbapp.service.ServiceStatus
import kotlinx.coroutines.*
import com.mthaler.knittings.utils.createNotificationChannel

class CompressPhotosService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CompressPhotosServiceManager.getInstance().updateServiceStatus(ServiceStatus.Started)
        val channelID = getString(R.string.compress_photos_notification_channel_id)
        createNotificationChannel(this, channelID, getString(R.string.compress_photos_notification_channel_name))

        val intent = Intent(this, CompressPhotosActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val initialNotification = createNotificationBuilder(pendingIntent, getString(R.string.compress_photos_notification_initial_msg)).build()

        startForeground(1, initialNotification)
        GlobalScope.launch {
            try {
                val cancelled = withContext(Dispatchers.IO) {
                    compressPhotos(pendingIntent)
                }
                val sm = CompressPhotosServiceManager.getInstance()
                val notificationManager = NotificationManagerCompat.from(this@CompressPhotosService)
                if (cancelled) {
                    sm.updateJobStatus(JobStatus.Cancelled(getString(R.string.compress_photos_cancelled)))
                    val n = createNotificationBuilder(pendingIntent, getString(R.string.compress_photos_notification_cancelled_msg)).build()
                    notificationManager.notify(1, n)
                } else {
                    sm.updateJobStatus(JobStatus.Success(getString(R.string.compress_photos_completed)))
                    val n = createNotificationBuilder(pendingIntent, getString(R.string.compress_photos_notification_done_msg)).build()
                    notificationManager.notify(1, n)
                }
            } finally {
                stopForeground(false)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // don't allow binding
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        CompressPhotosServiceManager.getInstance().updateServiceStatus(ServiceStatus.Stopped)
    }

    private suspend fun compressPhotos(pendingIntent: PendingIntent): Boolean {
        val builder = createNotificationBuilder(pendingIntent, getString(R.string.compress_photos_notification_initial_msg))
        val sm = CompressPhotosServiceManager.getInstance()
        val notificationManager = NotificationManagerCompat.from(this)
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
            val compressed = PictureUtils.compress(this@CompressPhotosService, file)
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
            builder.setProgress(100, progress, false)
            notificationManager.notify(1, builder.build())
            sm.updateJobStatus(JobStatus.Progress(progress))
        }
        return false
    }

    private fun createNotificationBuilder(pendingIntent: PendingIntent, msg: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, getString(R.string.compress_photos_notification_channel_id)).apply {
            setContentTitle(getString(R.string.compress_photos_notification_title))
            setContentText(msg)
            setSmallIcon(R.drawable.ic_photo_size_select_large_black_24dp)
            setContentIntent(pendingIntent)
            setDefaults(0)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_LOW
        }
    }

    companion object {

        fun startService(context: Context, message: String) {
            val startIntent = Intent(context, CompressPhotosService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, CompressPhotosService::class.java)
            context.stopService(stopIntent)
        }
    }
}
