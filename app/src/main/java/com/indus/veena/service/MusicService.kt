package com.indus.veena.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.CommandButton
import androidx.media3.session.CommandButton.ICON_STOP
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.indus.veena.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.indus.veena.R

@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService(), MediaSession.Callback {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val commandClose = "ACTION_CLOSE"
    @Inject lateinit var queueNavigation: QueueNavigation


    override fun onCreate() {
        super.onCreate()
        initializePlayer()
    }

    private val headersByUri = java.util.concurrent.ConcurrentHashMap<String, Map<String, String>>()

    private fun initializePlayer() {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(MediaCache.getCache(this))
            .setUpstreamDataSourceFactory(
                androidx.media3.datasource.ResolvingDataSource.Factory(
                    DefaultDataSource.Factory(this, httpDataSourceFactory)
                ) { spec ->
                    val headers = headersByUri[spec.uri.toString()].orEmpty()
                    spec.withAdditionalHeaders(headers)
                }
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
        val previous = CommandButton.Builder(CommandButton.ICON_PREVIOUS)
            .setDisplayName(getString(R.string.queue_previous))
            .setSessionCommand(SessionCommand(QueueNavigation.PREVIOUS, Bundle.EMPTY))
            .setSlots(CommandButton.SLOT_BACK)
            .build()
        val next = CommandButton.Builder(CommandButton.ICON_NEXT)
            .setDisplayName(getString(R.string.queue_next))
            .setSessionCommand(SessionCommand(QueueNavigation.NEXT, Bundle.EMPTY))
            .setSlots(CommandButton.SLOT_FORWARD)
            .build()
        val close = CommandButton.Builder(ICON_STOP)
            .setDisplayName(getString(R.string.close))
            .setSessionCommand(SessionCommand(commandClose, Bundle.EMPTY))
            .setSlots(CommandButton.SLOT_FORWARD_SECONDARY, CommandButton.SLOT_OVERFLOW)
            .build()

        mediaSession = MediaSession.Builder(this, player ?: return)
            .setSessionActivity(pendingIntent)
            .setMediaButtonPreferences(listOf(previous, next, close))
            .setCallback(this)
            .build()

        setMediaNotificationProvider(DefaultMediaNotificationProvider(this))
    }

    override fun onAddMediaItems(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        mediaItems.forEach { item ->
            val uri = item.localConfiguration?.uri ?: item.requestMetadata.mediaUri
            val supplied = item.requestMetadata.extras?.getBundle("stream_headers")
            if (uri != null) {
                if (headersByUri.size > 32) headersByUri.clear()
                headersByUri[uri.toString()] = supplied?.keySet()
                    ?.associateWith { supplied.getString(it).orEmpty() }.orEmpty()
            }
        }
        return Futures.immediateFuture(mediaItems)
    }

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val sessionCommands = connectionResult.availableSessionCommands
            .buildUpon()
            .add(SessionCommand(QueueNavigation.PREVIOUS, Bundle.EMPTY))
            .add(SessionCommand(QueueNavigation.NEXT, Bundle.EMPTY))
            .add(SessionCommand(commandClose, Bundle.EMPTY))
            .build()

        return MediaSession.ConnectionResult.accept(
            sessionCommands,
            Player.Commands.Builder().addAllCommands().build()
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
            QueueNavigation.PREVIOUS -> queueNavigation.moveBy(-1)
            QueueNavigation.NEXT -> queueNavigation.moveBy(1)
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
}
