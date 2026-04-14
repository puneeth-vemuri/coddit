package com.coddit.app.data.remote.fcm

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.coddit.app.CodditApp
import com.coddit.app.MainActivity
import com.coddit.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "New update"
        val body = remoteMessage.notification?.body ?: "Something happened on Coddit"
        val type = remoteMessage.data["type"] ?: CodditApp.CHANNEL_SOCIAL
        
        sendNotification(title, body, type)
    }

    private fun sendNotification(title: String, body: String, channelId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Refresh token logic
    }
}
