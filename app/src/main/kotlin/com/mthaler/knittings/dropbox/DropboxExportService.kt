package com.mthaler.knittings.dropbox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
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
import com.mthaler.knittings.service.Status
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
        createNotificationChannel()

        val intent = Intent(this, DropboxExportActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setOngoing(true)
            setContentTitle("Dropbox export")
            setContentText("Dropbox export in progress")
            setSmallIcon(R.drawable.ic_cloud_upload_black_24dp)
            setContentIntent(pendingIntent)
        }

        startForeground(1, builder.build())

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val wakeLock: PowerManager.WakeLock =
                        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Knittings::DropboxImport").apply {
                                acquire()
                            }
                        }
                try {
                    upload(builder)
                    DropboxExportServiceManager.getInstance().statusUpdated(Status.Success)
                } finally {
                    wakeLock.release()
                }
            }
            builder.setContentText("Dropbox import done")
            builder.setProgress(0, 0, false)
            stopForeground(true)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).let {
                val name = "dropbox export"
                val importance =  NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                it.createNotificationChannel(channel)
            }
        }
    }

    private fun upload(builder: NotificationCompat.Builder) {
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
            //if (isCancelled) break
            val inputStream = FileInputStream(photo.filename)
            dbxClient.files().uploadBuilder("/" + dir + "/" + photo.id + "." + getExtension(photo.filename.name)) // Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                    .uploadAndFinish(inputStream)
            val progress = (index / count.toFloat() * 100).toInt()
            builder.setProgress(100, progress, false)
            notificationManager.notify(1, builder.build())
            sm.statusUpdated(Status.Progress(progress))
        }
    }

    companion object {
        private val CHANNEL_ID = "com.mthaler.knittings.compressphotos.DropboxExportService"

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
