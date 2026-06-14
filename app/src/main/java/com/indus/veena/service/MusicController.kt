package com.indus.veena.service

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.indus.veena.models.SongModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicController @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context
) {
    var mediaController: MediaController? = null
    private val _closeEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val closeEvent = _closeEvent.asSharedFlow()
    private val connectionFuture = CompletableFuture<MediaController>()
    val ctx = context

    // Connection State
    private val _isConnected = MutableStateFlow(false)

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState = _playbackState.asStateFlow()

    private val _currentDuration = MutableStateFlow(0L)
    val currentDuration = _currentDuration.asStateFlow()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                val controller = mediaController ?: return@addListener
                connectionFuture.complete(controller)
                _isConnected.value = true

                // Sync initial state
                _isPlaying.value = controller.isPlaying
                _playbackState.value = controller.playbackState
                _currentDuration.value = controller.duration.coerceAtLeast(0L)

                // Listen for player changes globally
                controller.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        _errorFlow.tryEmit("Playback Error: ${error.message}")
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }

                    override fun onEvents(player: Player, events: Player.Events) {
                        if (!controller.isConnected) {
                            _closeEvent.tryEmit(Unit)
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _playbackState.value = playbackState
                        _currentDuration.value = controller.duration.coerceAtLeast(0L)
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        _currentDuration.value = controller.duration.coerceAtLeast(0L)
                    }

                    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                        _currentDuration.value = controller.duration.coerceAtLeast(0L)
                    }
                })
            } catch (e: Exception) {
                connectionFuture.completeExceptionally(e)
                _errorFlow.tryEmit("Failed to connect to MediaSession: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    suspend fun ensureConnected(): Boolean {
        if (mediaController != null && mediaController!!.isConnected) return true

        return try {
            // Wait up to 5 seconds for connection if not ready
            withContext(Dispatchers.IO) {
                connectionFuture.get(5, TimeUnit.SECONDS)
            }
            mediaController != null && mediaController!!.isConnected
        } catch (e: Exception) {
            _errorFlow.tryEmit("Connection timed out or failed.")
            false
        }
    }

    suspend fun playSong(url: String, song: SongModel) {
        if (!ensureConnected()) {
            _errorFlow.tryEmit("Cannot play: Service not connected.")
            return
        }

        withContext(Dispatchers.Main) {
            try {
                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setArtworkUri(song.thumbnail.toUri())
                            .build()
                    )
                    .build()

                mediaController?.let { controller ->
                    controller.setMediaItem(mediaItem)
                    controller.prepare()
                    controller.play()

                    // Force play if the service is in a weird state
                    if (!controller.playWhenReady) {
                        controller.playWhenReady = true
                    }
                }
            } catch (e: Exception) {
                _errorFlow.tryEmit("Failed to load song: ${e.message}")
            }
        }
    }

    fun togglePlayPause() {
        if (mediaController?.isConnected == true) {
            if (mediaController!!.isPlaying) mediaController?.pause()
            else mediaController?.play()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                if (ensureConnected()) togglePlayPause()
            }
        }
    }

    fun seekTo(position: Long) {
        if (mediaController?.isConnected == true) {
            mediaController?.seekTo(position)
        }
    }

    fun stopPlayer() {
        if (mediaController?.isConnected == true) {
            mediaController?.stop()
            mediaController?.clearMediaItems()
        }
    }
}