package com.mthaler.knittings.dropbox

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.R
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.model.Project
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName
import com.mthaler.knittings.utils.FileUtils.getExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.util.*

class DropboxExportService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

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
            val dir = createDateTimeDirectoryName(Date())
            val cancelled = withContext(Dispatchers.IO) {
                val wakeLock: PowerManager.WakeLock =
                    (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                        newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK,
                            "Knittings::DropboxExport"
                        ).apply {
                            acquire()
                        }
                    }
                try {
                    upload(dir, pendingIntent)
                } finally {
                    wakeLock.release()
                }
            }
            onUploadCompleted(dir, cancelled, pendingIntent)
            stopForeground(false)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    private suspend fun upload(dir: String, pendingIntent: PendingIntent): Boolean {
        val clientIdentifier = "Knittings"
        val requestConfig = DbxRequestConfig(clientIdentifier)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val builder = createNotificationBuilder(
                    pendingIntent,
                    getString(R.string.dropbox_export_notification_initial_msg)
                )
                val notificationManager = NotificationManagerCompat.from(this)
                val sm = DropboxExportServiceManager.getInstance()
                // create directory containing current date & time
                dropboxClient.files().createFolderV2("/$dir")
                val database = (applicationContext as DatabaseApplication<Project>).createExportDatabase().checkDatabase()
                uploadDatabase(dropboxClient, dir, database)
                // upload photos to dropbox
                val count = database.photos.size
                for ((index, photo) in database.photos.withIndex()) {
                    if (sm.cancelled) {
                        return true
                    }
                    uploadPhoto(dropboxClient, dir, photo)
                    val progress = (index / count.toFloat() * 100).toInt()
                    builder.setProgress(100, progress, false)
                    notificationManager.notify(1, builder.build())
                    sm.updateJobStatus(JobStatus.Progress(progress))
                }
            } else {
                val sm = DropboxExportServiceManager.getInstance()
                 dropboxClient.files().createFolderV2("/$dir")
                 val database = (applicationContext as DatabaseApplication<Project>).createExportDatabase().checkDatabase()
                 uploadDatabase(dropboxClient, dir, database)
                 // upload photos to dropbox
                 val count = database.photos.size
                 for ((index, photo) in database.photos.withIndex()) {
                     if (sm.cancelled) {
                        return true
                    }
                    uploadPhoto(dropboxClient, dir, photo)
                    val progress = (index / count.toFloat() * 100).toInt()
                    sm.updateJobStatus(JobStatus.Progress(progress))
                 }
            }
        }
        return false
    }

    private fun uploadDatabase(dbxClient: DbxClientV2, dir: String, database: ExportDatabase<Project>) {
        val dbJSON = database.toJSON()
        val s = dbJSON.toString(2)
        val dbInputStream = ByteArrayInputStream(s.toByteArray())
        // upload database to dropbox
        dbxClient.files().uploadBuilder("/$dir/db.json") // Path in the user's Dropbox to save the file.
                .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                .uploadAndFinish(dbInputStream)
    }

    private fun uploadPhoto(dbxClient: DbxClientV2, dir: String, photo: Photo) {
        val inputStream = FileInputStream(photo.filename)
        dbxClient.files().uploadBuilder("/" + dir + "/" + photo.id + "." + getExtension(photo.filename.name)) // Path in the user's Dropbox to save the file.
                .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                .uploadAndFinish(inputStream)
    }

    private fun onUploadCompleted(dir: String, cancelled: Boolean, pendingIntent: PendingIntent) {
        val nm = NotificationManagerCompat.from(this@DropboxExportService)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (cancelled) {
                val n = createNotificationBuilder(
                    pendingIntent,
                    getString(R.string.dropbox_export_notification_cancelled_msg),
                    false).build()
                nm.notify(1, n)
                DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Cancelled(getString(R.string.dropbox_export_notification_cancelled_msg), dir))
            } else {
                val n = createNotificationBuilder(pendingIntent, getString(R.string.dropbox_export_notification_done_msg), false).build()
                nm.notify(1, n)
                DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Success())
            }
        } else {
            DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Cancelled(getString(R.string.dropbox_export_notification_cancelled_msg), dir))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationBuilder(pendingIntent: PendingIntent, msg: String, autoCancel: Boolean = true): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, getString(R.string.dropbox_export_notification_channel_id)).apply {
            setContentTitle(getString(R.string.dropbox_export_notification_title))
            setContentText(msg)
            // vector icons crash the service on Android < 21
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setSmallIcon(R.drawable.ic_cloud_upload_black_24dp)
            }
            setContentIntent(pendingIntent)
            setDefaults(0)
            setAutoCancel(autoCancel)
            priority = NotificationCompat.PRIORITY_LOW
        }
    }

    //deserialize the credential from SharedPreferences if it exists
    private fun getLocalCredential(): DbxCredential? {
        val sharedPreferences = getSharedPreferences(DropboxImportService.KNITTINGS, Activity.MODE_PRIVATE)
        val serializedCredential = sharedPreferences.getString("credential", null) ?: return null
        return DbxCredential.Reader.readFully(serializedCredential)
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

