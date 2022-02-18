package com.mthaler.knittings.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

fun createNotificationChannel(context: Context, id: String, name: String, description: String? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
                id,
                name,
                NotificationManager.IMPORTANCE_LOW
        )
        if (description != null) {
            notificationChannel.description = description
        }

        val notificationManager = context.getSystemService(
                NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }
}