package com.example.doanmess

import android.app.Application

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        createChannel()
        instance = this
    }
    companion object {
        lateinit var instance: MyApp
            private set
    }
    private fun createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "MESSAGE",
                "Message",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            val channel2 = android.app.NotificationChannel(
                "CALL_CHANNEL",
                "Call Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel2)
        }
    }

}