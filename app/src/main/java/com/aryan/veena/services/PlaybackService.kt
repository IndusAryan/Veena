package com.aryan.veena.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.aryan.veena.ui.activities.MainActivity
//import com.aryan.veena.utils.NowPlayingNotification
import kotlin.reflect.typeOf

/*class PlaybackService : MediaSessionService() {

    public var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(getPendingIntent())
            .build()

    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        return PendingIntent.getActivity(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NowPlayingNotification.NOTIFICATION_ID)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        // Cancel the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NowPlayingNotification.NOTIFICATION_ID)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    inner class PlaybackServiceBinder(private val service: PlaybackService) : Binder() {
        fun getService(): PlaybackService { return service }
        fun getMediaSession(): MediaSession? { return service.mediaSession }
    }
}
*/
