package com.mthaler.knittings.dropbox

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.R
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Project
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import kotlinx.coroutines.*

class DropboxImportService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DropboxImportServiceManager.getInstance().updateServiceStatus(ServiceStatus.Started)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

            createNotificationBuilder(pendingIntent, getString(R.string.dropbox_import_notification_channel_name))
        }

        val directory = intent?.getStringExtra(EXTRA_DIRECTORY)
        val database = intent?.getParcelableExtra<ExportDatabase<Project>>(EXTRA_DATABASE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val initialNotification = createNotificationBuilder(
                pendingIntent,
                getString(R.string.dropbox_import_notification_initial_msg)
            ).build()

            startForeground(1, initialNotification)
        } else {
            startForeground(1, Notification())
        }

        GlobalScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (directory != null && database != null) {
                        val wakeLock: PowerManager.WakeLock =
                            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Knittings::DropboxImport").apply {
                                    acquire()
                                }
                            }
                        try {
                            downloadPhotos(database, directory, pendingIntent)
                        } finally {
                            wakeLock.release()
                        }
                    }
                }
                DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Success(getString(R.string.dropbox_import_completed)))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val n = createNotificationBuilder(pendingIntent, getString(R.string.dropbox_import_notification_done_msg), false).build()
                    NotificationManagerCompat.from(this@DropboxImportService).notify(1, n)
                }
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

    private suspend fun downloadPhotos(database: ExportDatabase<Project>, directory: String, pendingIntent: PendingIntent) {
        val clientIdentifier = "Knittings"
        val requestConfig = DbxRequestConfig(clientIdentifier)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val sm = DropboxImportServiceManager.getInstance()
            database.write(this, dropboxClient, directory) { progress ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val builder = createNotificationBuilder(pendingIntent,  getString(R.string.dropbox_import_notification_initial_msg))
                    val notificationManager = NotificationManagerCompat.from(this)
                    builder.setProgress(100, progress, false)
                    notificationManager.notify(1, builder.build())
                }
                sm.updateJobStatus(JobStatus.Progress(progress))
            }
        }
    }

    //deserialize the credential from SharedPreferences if it exists
    protected fun getLocalCredential(): DbxCredential? {
        val sharedPreferences = getSharedPreferences(KNITTINGS, Activity.MODE_PRIVATE)
        val serializedCredential = sharedPreferences.getString("credential", null) ?: return null
        return DbxCredential.Reader.readFully(serializedCredential)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = getString(R.string.dropbox_import_notification_channel_name)
        val descriptionText = getString(R.string.dropbox_import_notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(getString(R.string.dropbox_import_notification_channel_id), name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    @RequiresApi(Build.VERSION_CODES.O)
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

        val KNITTINGS = "com.mthaler.knittings"

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