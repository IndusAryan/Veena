package com.aryan.veena.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.media3.common.util.UnstableApi
import com.aryan.veena.R

/*object NowPlayingNotification {

    private const val CHANNEL_ID = "media_playback_channel"
    const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Media_Playback",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Channel for media playback"
            val manager = getSystemService(context, NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun createMediaNotification(
        context: Context,
        songTitle: String,
        thumbnail: Bitmap?,
    ): Notification {

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(songTitle)
            .setLargeIcon(thumbnail)
            .setColorized(true)
            .setColor(Color.WHITE)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    fun showNotification(context: Context, notification: Notification) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }
}*/
