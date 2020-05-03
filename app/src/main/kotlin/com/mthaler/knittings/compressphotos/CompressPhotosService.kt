package com.mthaler.knittings.compressphotos

import android.app.*
import android.content.Intent
import android.content.Context
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mthaler.knittings.R
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
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

        val notificationManager = NotificationManagerCompat.from(this);
        val builder = NotificationCompat.Builder(this, channelID).apply {
            setOngoing(true)
            setContentTitle("Compress Photos")
            setContentText("Compressing photos in progress")
            setSmallIcon(R.drawable.ic_photo_size_select_large_black_24dp)
            setContentIntent(pendingIntent)
            setPriority(NotificationCompat.PRIORITY_LOW)
        }
        startForeground(1, builder.build())
        GlobalScope.launch {
            try {
                val sm = CompressPhotosServiceManager.getInstance()
                withContext(Dispatchers.Default) {
                    for(i in 1..10) {
                        if (sm.canceled) break
                        delay(2000)
                        builder.setProgress(100, i * 10, false)
                        notificationManager.notify(1, builder.build())
                        sm.updateJobStatus(JobStatus.Progress(i * 10))
                    }
                    CompressPhotosServiceManager.getInstance().updateJobStatus(JobStatus.Success(getString(R.string.compress_photos_completed)))
                }
                builder.setContentText("Compressing photos done")
                builder.setProgress(0, 0, false)
            } finally {
                stopForeground(true)
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
