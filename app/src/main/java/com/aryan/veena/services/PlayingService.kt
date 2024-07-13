package com.aryan.veena.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.aryan.veena.R
import com.aryan.veena.utils.CustomNotificationControls
import com.aryan.veena.utils.CustomNotificationControls.REWIND
import com.aryan.veena.utils.CustomNotificationControls.CLOSE
import com.aryan.veena.utils.CustomNotificationControls.FORWARD
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlayingService : MediaSessionService(), MediaSession.Callback {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    private val notificationControls =
        CustomNotificationControls.entries.map { command -> command.commandButton }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(this, { 108 }, "PLAYING_SERVICE", R.string.app_name)
                .apply {
                    setSmallIcon(R.drawable.ic_launcher_foreground)
                }
        )

        player = ExoPlayer.Builder(this).build().apply {
            playWhenReady = true
            player.apply {
                setWakeMode(C.WAKE_MODE_NETWORK)
                /*skipSilenceEnabled = false*/ //TODO make this a setting
                setHandleAudioBecomingNoisy(true)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
            }

            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("PlayingService", "Player error: ${error.message}")
                }

                override fun onPlaybackStateChanged(state: Int) {
                    Log.d("PlayingService", "Playback state changed: $state")
                }
            })
        }

        mediaSession = MediaSession.Builder(this, player ?: return)
            .setSessionActivity(createContentIntent())
            .setCustomLayout(notificationControls)
            .setCallback(this)
            .build()
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            null
        }
        super.onDestroy()
    }

    // The user dismissed the app from the recent tasks
    override fun onTaskRemoved(rootIntent: Intent?) {
        closeService()
    }

    /**
    https://medium.com/@a.poplawski96/customising-jetpack-media3-player-notification-chapter-i-adding-custom-commands-3cd16256e0e0
     **/
    // custom notification commands
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()

        /* Registering custom player command buttons for player notification. */
        notificationControls.forEach { commandButton ->
            commandButton.sessionCommand?.let(availableSessionCommands::add)
        }

        return MediaSession.ConnectionResult.accept(
            availableSessionCommands.build(),
            connectionResult.availablePlayerCommands
        )
    }

    override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
        super.onPostConnect(session, controller)
            /* Setting custom player command buttons to mediaLibrarySession for player notification. */
            mediaSession?.setCustomLayout(notificationControls)
    }

    private fun closeService() {
        player?.release()
        mediaSession?.release()
        // Send broadcast that the service is closing
        val intent = Intent("com.aryan.veena.PLAYING_SERVICE_CLOSED")
        sendBroadcast(intent)
        Log.d("PlayingService", "Broadcast sent: PLAYING_SERVICE_CLOSED")
        stopSelf()
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (customCommand.customAction) {
            REWIND.customAction -> session.player.seekBack()
            FORWARD.customAction -> session.player.seekForward()
            CLOSE.customAction -> closeService()
        }
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    private fun createContentIntent(): PendingIntent {
        val intent = packageManager?.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}