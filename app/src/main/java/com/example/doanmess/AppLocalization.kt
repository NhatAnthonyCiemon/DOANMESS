package com.example.doanmess

import android.app.Application

class AppLocalization: Application() {
    companion object {
        lateinit var instance: AppLocalization
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}