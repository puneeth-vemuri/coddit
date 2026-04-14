package com.coddit.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CodditApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_REPLIES,
                    "Replies",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for new replies to your posts"
                },
                NotificationChannel(
                    CHANNEL_BYTES,
                    "Bytes Rewards",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications when you earn Bytes"
                },
                NotificationChannel(
                    CHANNEL_SOCIAL,
                    "Social activity",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "General social updates"
                }
            )

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(channels)
        }
    }

    companion object {
        const val CHANNEL_REPLIES = "replies"
        const val CHANNEL_BYTES = "bytes"
        const val CHANNEL_SOCIAL = "social"
    }
}
