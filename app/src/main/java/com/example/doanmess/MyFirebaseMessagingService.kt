package com.example.doanmess

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.createuiproject.MainChat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title ?: "Default Title"
        val message = remoteMessage.notification?.body ?: "Default Message"

        // Gọi hàm hiển thị thông báo
        showHighPriorityNotification(this, title, message)
    }
    override fun onNewToken(token: String) {
        Log.e("HHHHHHHHHHHHHHHHHHHHHHHH", "Refreshed token: $token")
        //sendRegistrationToServer(token)
    }
    private fun showHighPriorityNotification(context: Context, title: String, message: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val channelId = "TIN_NHAN_MOI"
            val bitmapAvatar = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.avatar_placeholder_allchat
            )
            val intent = Intent(context, MainChat::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.checkmark2)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setLargeIcon(bitmapAvatar)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            with(NotificationManagerCompat.from(context)) {
                notify(getTimeCurrent(), builder.build())
            }
        }
    }

    fun getTimeCurrent(): Int {
        return System.currentTimeMillis().toInt()
    }
}