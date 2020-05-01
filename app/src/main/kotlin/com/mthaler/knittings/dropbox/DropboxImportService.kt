package com.mthaler.knittings.dropbox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.utils.FileUtils
import com.mthaler.knittings.utils.PictureUtils
import kotlinx.coroutines.*
import java.io.FileOutputStream
import java.lang.Exception

class DropboxImportService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val database = intent?.getParcelableExtra<Database>(EXTRA_DATABASE)
        val directory = intent?.getStringExtra(EXTRA_DIRECTORY)

        val intent = Intent(this, DropboxImportActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setOngoing(true)
            setContentTitle("Dropbox import")
            setContentText("Dropbox import in progress")
            setSmallIcon(R.drawable.ic_photo_size_select_large_black_24dp)
            setContentIntent(pendingIntent)
        }


        startForeground(1, builder.build())

        GlobalScope.launch {
            val sm = DropboxImportServiceManager.getInstance()
            withContext(Dispatchers.Default) {
                if (database != null && directory != null) {
                    downloadPhotos(database, directory, builder)
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
                val name = "dropbox import"
                val importance =  NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance)
                it.createNotificationChannel(channel)
            }
        }
    }

    private fun downloadPhotos(database: Database, directory: String, builder: NotificationCompat.Builder) {
        val notificationManager = NotificationManagerCompat.from(this);
        val sm = DropboxImportServiceManager.getInstance()
        val count = database.photos.size
        val dbxClient = DropboxClientFactory.getClient()
        for ((index, photo) in database.photos.withIndex()) {
            try {
                // Download the file.
                val filename = "/" + directory + "/" + photo.id + "." + FileUtils.getExtension(photo.filename.name)
                FileOutputStream(photo.filename).use {
                    dbxClient.files().download(filename).download(it)
                }
                // generate preview
                val orientation = PictureUtils.getOrientation(photo.filename.absolutePath)
                val preview = PictureUtils.decodeSampledBitmapFromPath(photo.filename.absolutePath, 200, 200)
                val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
                val photoWithPreview = photo.copy(preview = rotatedPreview)
                this.datasource.updatePhoto(photoWithPreview)
                val progress = (index / count.toFloat() * 100).toInt()
                builder.setProgress(100, progress, false)
                notificationManager.notify(1, builder.build())
                sm.statusUpdated(Status.Progress(progress))
            } catch(excetion: Exception) {
                sm.statusUpdated(Status.Error(excetion))
            }
        }
    }

    companion object {
        private val CHANNEL_ID = "com.mthaler.knittings.compressphotos.DropboxImportService"
        private val EXTRA_DATABASE = "database"
        private val EXTRA_DIRECTORY = "directory"

        fun startService(context: Context, database: Database, directory: String) {
            val startIntent = Intent(context, DropboxImportService::class.java)
            startIntent.putExtra(EXTRA_DATABASE, database as Parcelable)
            startIntent.putExtra(EXTRA_DIRECTORY, directory)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, DropboxImportService::class.java)
            context.stopService(stopIntent)
        }
    }
}
