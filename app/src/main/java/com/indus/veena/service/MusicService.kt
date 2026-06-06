package com.indus.veena.service

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint

@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService(), MediaSession.Callback {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

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

        mediaSession = MediaSession.Builder(this, player ?: return)
            .setCallback(this)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        player?.pause()
        if (player?.isPlaying == true) { releaseAndStop() }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        releaseAndStop()
        super.onDestroy()
    }

    private fun releaseAndStop() {
        player?.stop()
        mediaSession?.release()
        mediaSession = null
        player?.release()
        player = null
        stopSelf()
    }
}