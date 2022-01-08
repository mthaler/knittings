package com.mthaler.knittings.dropbox

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mthaler.knittings.R
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Project
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import kotlinx.coroutines.*

class DropboxImportService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DropboxImportServiceManager.getInstance().updateServiceStatus(ServiceStatus.Started)
        val channelID = getString(R.string.dropbox_import_notification_channel_id)
        com.mthaler.knittings.utils.createNotificationChannel(
            this,
            channelID,
            getString(R.string.dropbox_import_notification_channel_name)
        )

        val directory = intent?.getStringExtra(EXTRA_DIRECTORY)!!
        val database = intent?.getParcelableExtra<ExportDatabase<Project>>(EXTRA_DATABASE)

        val intent = Intent(this, DropboxImportActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val initialNotification = createNotificationBuilder(pendingIntent, getString(R.string.dropbox_import_notification_initial_msg)).build()

        startForeground(1, initialNotification)

        GlobalScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (directory != null) {
                        val wakeLock: PowerManager.WakeLock =
                                (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Knittings::DropboxImport").apply {
                                        acquire()
                                    }
                                }
                        try {
                            downloadPhotos(database!!, directory!!, pendingIntent)
                        } finally {
                            wakeLock.release()
                        }
                    }
                }
                DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Success(getString(R.string.dropbox_import_completed)))
                val n = createNotificationBuilder(pendingIntent, getString(R.string.dropbox_import_notification_done_msg), false).build()
                NotificationManagerCompat.from(this@DropboxImportService).notify(1, n)
            } finally {
                stopForeground(false)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        DropboxImportServiceManager.getInstance().updateServiceStatus(ServiceStatus.Stopped)
    }

    private fun downloadPhotos(database: ExportDatabase<Project>, directory: String, pendingIntent: PendingIntent) {
        val builder = createNotificationBuilder(pendingIntent, getString(R.string.dropbox_import_notification_initial_msg))
        val notificationManager = NotificationManagerCompat.from(this)
        val sm = DropboxImportServiceManager.getInstance()
        val dbxClient =
            DropboxClientFactory.getClient()
        database.write(dbxClient, directory) { progress ->
            builder.setProgress(100, progress, false)
            notificationManager.notify(1, builder.build())
            sm.updateJobStatus(JobStatus.Progress(progress))
        }
    }

    private fun createNotificationBuilder(pendingIntent: PendingIntent, msg: String, autoCancel: Boolean = true): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, getString(R.string.dropbox_import_notification_channel_id)).apply {
            setContentTitle(getString(R.string.dropbox_import_notification_title))
            setContentText(msg)
            // vector icons crash the service on Android < 21
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setSmallIcon(R.drawable.ic_cloud_download_black_24dp)
            }
            setContentIntent(pendingIntent)
            setDefaults(0)
            setAutoCancel(autoCancel)
            priority = NotificationCompat.PRIORITY_LOW
        }
    }

    companion object {
        private val EXTRA_DIRECTORY = "directory"
        private val EXTRA_DATABASE = "database"

        fun startService(context: Context, directory: String, database: ExportDatabase<Project>) {
            val startIntent = Intent(context, DropboxImportService::class.java)
            startIntent.putExtra(EXTRA_DIRECTORY, directory)
            startIntent.putExtra(EXTRA_DATABASE, database as Parcelable)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, DropboxImportService::class.java)
            context.stopService(stopIntent)
        }
    }
}