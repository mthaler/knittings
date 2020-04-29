package com.mthaler.knittings.compressphotos

import android.app.*
import android.content.Intent
import android.content.Context
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mthaler.knittings.R
import kotlinx.coroutines.*

class CompressPhotosService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //do heavy work on a background thread
        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, CompressPhotosActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setOngoing(true)
                .setContentTitle("Compress Photos Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_photo_size_select_large_black_24dp)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)
        GlobalScope.launch {
            withContext(Dispatchers.Default) {
                delay(5000)
                ServiceManager.getInstance().statusUpdated(Status.Success)
            }
            stopForeground(true)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).let {
                if (it.getNotificationChannel(CHANNEL_ID) == null) {
                    it.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Compress Photos Channel", NotificationManager.IMPORTANCE_DEFAULT))
                }
            }
        }
    }

    companion object {

        private val CHANNEL_ID = "com.mthaler.knittings.compressphotos"

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
