package com.aryan.veena

import android.app.Application
import com.aryan.veena.utils.NowPlayingNotificatio.createNotificationChannel

class VeenaApp: Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(applicationContext)
    }
}