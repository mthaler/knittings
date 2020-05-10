package com.mthaler.knittings.dropbox

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dropbox.core.v2.files.WriteMode
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.toJSON
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName
import com.mthaler.knittings.utils.FileUtils.getExtension
import com.mthaler.knittings.utils.createNotificationChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.util.*

class DropboxExportService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelID = getString(R.string.dropbox_export_notification_channel_id)
        createNotificationChannel(this, channelID, getString(R.string.dropbox_export_notification_channel_name))

        val intent = Intent(this, DropboxExportActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val initialNotification = createNotificationBuilder(pendingIntent, getString(R.string.dropbox_export_notification_initial_msg)).build()

        startForeground(1, initialNotification)

        GlobalScope.launch {
            val cancelled = withContext(Dispatchers.IO) {
                val wakeLock: PowerManager.WakeLock =
                        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Knittings::DropboxExport").apply {
                                acquire()
                            }
                        }
                try {
                    upload(pendingIntent)
                } finally {
                    wakeLock.release()
                }
            }
            val nm = NotificationManagerCompat.from(this@DropboxExportService)
            if (cancelled) {
                val n = createNotificationBuilder(pendingIntent, getString(R.string.dropbox_export_notification_cancelled_msg), false).build()
                nm.notify(1, n)
                DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Cancelled("Dropbox export canceled"))
            } else {
                val n = createNotificationBuilder(pendingIntent, getString(R.string.dropbox_export_notification_done_msg), false).build()
                nm.notify(1, n)
                DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Success())
            }
            stopForeground(false)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun upload(pendingIntent: PendingIntent): Boolean {
        val builder = createNotificationBuilder(pendingIntent, getString(R.string.dropbox_export_notification_initial_msg))
        val dbxClient = DropboxClientFactory.getClient()
        val notificationManager = NotificationManagerCompat.from(this);
        val sm = DropboxExportServiceManager.getInstance()
        val knittings = datasource.allKnittings
        val photos = datasource.allPhotos
        val categories = datasource.allCategories
        val needles = datasource.allNeedles
        val dbJSON = Database(knittings, photos, categories, needles).toJSON()
        val s = dbJSON.toString(2)
        val dbInputStream = ByteArrayInputStream(s.toByteArray())

        // create directory containing current date & time
        val dir = createDateTimeDirectoryName(Date())
        dbxClient.files().createFolderV2("/$dir")

        // upload database to dropbox
        dbxClient.files().uploadBuilder("/$dir/db.json") // Path in the user's Dropbox to save the file.
                .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                .uploadAndFinish(dbInputStream)

        // upload photos to dropbox
        val count = photos.size
        for ((index, photo) in photos.withIndex()) {
            if (sm.cancelled) {
                return true
            }
            val inputStream = FileInputStream(photo.filename)
            dbxClient.files().uploadBuilder("/" + dir + "/" + photo.id + "." + getExtension(photo.filename.name)) // Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                    .uploadAndFinish(inputStream)
            val progress = (index / count.toFloat() * 100).toInt()
            builder.setProgress(100, progress, false)
            notificationManager.notify(1, builder.build())
            sm.updateJobStatus(JobStatus.Progress(progress))
        }
        return false
    }

    private fun createNotificationBuilder(pendingIntent: PendingIntent, msg: String, autoCancel: Boolean = true): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, getString(R.string.dropbox_export_notification_channel_id)).apply {
            setContentTitle(getString(R.string.dropbox_export_notification_title))
            setContentText(msg)
            setSmallIcon(R.drawable.ic_cloud_upload_black_24dp)
            setContentIntent(pendingIntent)
            setDefaults(0)
            setAutoCancel(autoCancel)
            priority = NotificationCompat.PRIORITY_LOW
        }
    }

    companion object {

        fun startService(context: Context) {
            val startIntent = Intent(context, DropboxExportService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, DropboxExportService::class.java)
            context.stopService(stopIntent)
        }
    }
}
