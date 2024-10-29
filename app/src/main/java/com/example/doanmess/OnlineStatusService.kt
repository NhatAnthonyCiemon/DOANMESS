package com.example.doanmess

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class OnlineStatusService : Service() {

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    override fun onCreate() {
        super.onCreate()
        updateOnlineStatus(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateOnlineStatus(false)
    }

    private fun updateOnlineStatus(isOnline: Boolean) {
        user?.let {
            val database = FirebaseDatabase.getInstance()
            val userStatusRef = database.getReference("users/${it.uid}/online")
            userStatusRef.setValue(isOnline)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}