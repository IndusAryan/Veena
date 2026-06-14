package com.indus.veena.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.CommandButton.ICON_SKIP_BACK_10
import androidx.media3.session.CommandButton.ICON_SKIP_FORWARD_10
import androidx.media3.session.CommandButton.ICON_STOP
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.indus.veena.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService(), MediaSession.Callback {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val commandClose = "ACTION_CLOSE"
    private val commandRewind = "ACTION_REWIND"
    private val commandForward = "ACTION_FORWARD"


    override fun onCreate() {
        super.onCreate()
        initializePlayer()
    }

    private fun initializePlayer() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(MediaCache.getCache(this))
            .setUpstreamDataSourceFactory(
                DefaultDataSource.Factory(this, httpDataSourceFactory)
            )

        player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(cacheDataSourceFactory)
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("MusicService", "ExoPlayer Error: ${error.message}", error)
                if (error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS) {
                    Log.e("MusicService", "HTTP 403 detected in service!")
                }
            }
        })

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val closeSessionCommand = SessionCommand(commandClose, Bundle.EMPTY)
        val rewindSessionCommand = SessionCommand(commandRewind, Bundle.EMPTY)
        val forwardSessionCommand = SessionCommand(commandForward, Bundle.EMPTY)

        val rewindButton = buildCommandButton(
            displayName = "Rewind 10s",
            iconConstant = ICON_SKIP_BACK_10,
            sessionCommand = rewindSessionCommand
        )
        val forwardButton = buildCommandButton(
            displayName = "Forward 10s",
            iconConstant = ICON_SKIP_FORWARD_10,
            sessionCommand = forwardSessionCommand
        )
        val closeButton = buildCommandButton(
            displayName = "Close",
            iconConstant = ICON_STOP,
            sessionCommand = closeSessionCommand
        )

        mediaSession = MediaSession.Builder(this, player ?: return)
            .setSessionActivity(pendingIntent)
            .setCallback(this)
            .build()

        // Set the custom layout containing your custom commands
        mediaSession?.setCustomLayout(listOf(rewindButton, forwardButton, closeButton))
        setMediaNotificationProvider(MusicNotificationProvider(this))
    }

    private fun buildCommandButton(
        displayName: String,
        iconConstant: Int,
        playerCommand: Int? = null,
        sessionCommand: SessionCommand? = null
    ): CommandButton {
        val builder = CommandButton.Builder(iconConstant).setDisplayName(displayName)
        playerCommand?.let { builder.setPlayerCommand(it) }
        sessionCommand?.let { builder.setSessionCommand(it) }
        return builder.build()
    }

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)

        // Add all custom commands to the available session commands
        val sessionCommands = connectionResult.availableSessionCommands
            .buildUpon()
            .add(SessionCommand(commandRewind, Bundle.EMPTY))
            .add(SessionCommand(commandForward, Bundle.EMPTY))
            .add(SessionCommand(commandClose, Bundle.EMPTY))
            .build()

        return MediaSession.ConnectionResult.accept(
            sessionCommands,
            connectionResult.availablePlayerCommands
        )
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        // Handle the custom actions when buttons are clicked
        when (customCommand.customAction) {
            commandRewind -> player?.seekBack()
            commandForward -> player?.seekForward()
            commandClose -> releaseAndStop()
        }
        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        releaseAndStop()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        releaseAndStop()
        super.onDestroy()
    }

    private fun releaseAndStop() {
        player?.stop()
        player?.clearMediaItems()
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        stopSelf()
    }

    @UnstableApi
    inner class MusicNotificationProvider(context: Context) : DefaultMediaNotificationProvider(context) {
        override fun getMediaButtons(
            session: MediaSession,
            playerCommands: Player.Commands,
            customLayout: ImmutableList<CommandButton>,
            showPauseButton: Boolean
        ): ImmutableList<CommandButton> {

            val playPause = CommandButton.Builder(
                if (showPauseButton) CommandButton.ICON_PAUSE else CommandButton.ICON_PLAY
            )
                .setPlayerCommand(Player.COMMAND_PLAY_PAUSE)
                .setDisplayName(if (showPauseButton) "Pause" else "Play")
                .setEnabled(true)
                .build()

            // Retrieve your custom buttons from the customLayout list
            val rewind = customLayout.firstOrNull { it.sessionCommand?.customAction == commandRewind }
            val forward = customLayout.firstOrNull { it.sessionCommand?.customAction == commandForward }
            val close = customLayout.firstOrNull { it.sessionCommand?.customAction == commandClose }

            return ImmutableList.copyOf(listOfNotNull(rewind, playPause, forward, close))
        }
    }
}
