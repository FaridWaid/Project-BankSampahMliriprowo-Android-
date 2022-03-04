package com.faridwaid.banksampahmliriprowo.admin

import com.google.firebase.messaging.FirebaseMessagingService
import androidx.core.app.NotificationManagerCompat
import android.R
import android.app.NotificationManager
import android.app.NotificationChannel
import com.google.firebase.messaging.RemoteMessage
import android.annotation.SuppressLint
import android.app.Notification
import android.graphics.Color
import androidx.core.app.NotificationCompat

class PushNotificationService : FirebaseMessagingService() {
    // Merupakan kelas yang digunakan untuk mendifinisikan fitur notifikasi
    @SuppressLint("NewApi")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification!!.title
        val text = remoteMessage.notification!!.body
        val CHANNEL_ID = "MESSAGE"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Message Notification",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification: Notification.Builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_popup_reminder)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(Notification.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setColor(Color.BLUE)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(1, notification.build())
        super.onMessageReceived(remoteMessage)
    }
}