package com.indus.veena.service

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.indus.veena.helpers.ImageModuleCoil.getCachedArtworkBlob
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
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
import kotlinx.coroutines.channels.BufferOverflow
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
    private val artworkScope = CoroutineScope(Dispatchers.Main)
    private var artworkJob: Job? = null
    private var selectionId = 0L
    var mediaController: MediaController? = null
    private val _closeEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val closeEvent = _closeEvent.asSharedFlow()
    private var connectionFuture: CompletableFuture<MediaController>? = null
    val ctx = context

    private val _errorFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errorFlow = _errorFlow.asSharedFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState = _playbackState.asStateFlow()

    private val _currentDuration = MutableStateFlow(0L)
    val currentDuration = _currentDuration.asStateFlow()

    @OptIn(UnstableApi::class)
    private fun connect(): CompletableFuture<MediaController> {
        val connection = CompletableFuture<MediaController>()
        connectionFuture = connection
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken)
            .setListener(object : MediaController.Listener {
                override fun onDisconnected(controller: MediaController) {
                    if (mediaController !== controller) return
                    mediaController = null
                    connectionFuture = null
                    _isPlaying.value = false
                    _playbackState.value = Player.STATE_IDLE
                    _currentDuration.value = 0L
                    _closeEvent.tryEmit(Unit)
                }
            })
            .buildAsync()

        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                val controller = mediaController ?: return@addListener
                connection.complete(controller)

                // Sync initial state
                _isPlaying.value = controller.isPlaying
                _playbackState.value = controller.playbackState
                _currentDuration.value = controller.duration.coerceAtLeast(0L)

                // Listen for player changes globally
                controller.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("MusicController", "onPlayerError: ${error.errorCodeName}", error)
                        val httpError = generateSequence<Throwable>(error) { it.cause }
                            .filterIsInstance<androidx.media3.datasource.HttpDataSource.InvalidResponseCodeException>()
                            .firstOrNull()
                        // MediaSession may reconstruct the remote cause as a generic exception.
                        val status = httpError?.responseCode ?: StreamFailurePolicy.httpStatus(error)
                        val message = if (status != null) context.getString(com.indus.veena.R.string.playback_http_error, status, httpError?.responseMessage.orEmpty())
                            else context.getString(com.indus.veena.R.string.playback_error, error.localizedMessage ?: context.getString(com.indus.veena.R.string.source_error))
                        _errorFlow.tryEmit(message)
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }

                    override fun onEvents(player: Player, events: Player.Events) {
                        _isPlaying.value = player.isPlaying
                        _playbackState.value = player.playbackState
                        _currentDuration.value = player.duration.coerceAtLeast(0L)
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
                connectionFuture = null
                connection.completeExceptionally(e)
                _errorFlow.tryEmit(context.getString(com.indus.veena.R.string.player_connect_failed, e.message.orEmpty()))
            }
        }, ContextCompat.getMainExecutor(context))
        return connection
    }

    private suspend fun ensureConnected(): Boolean = withContext(Dispatchers.Main.immediate) {
        if (mediaController?.isConnected == true) return@withContext true
        val connection = connectionFuture ?: connect()

        try {
            // Wait up to 5 seconds for connection if not ready
            withContext(Dispatchers.IO) {
                connection.get(5, TimeUnit.SECONDS)
            }
            mediaController?.isConnected == true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _errorFlow.tryEmit(context.getString(com.indus.veena.R.string.player_connect_timeout))
            false
        }
    }

    suspend fun playSong(url: String, song: SongModel) {
        val requestId = ++selectionId
        artworkJob?.cancel()
        if (!ensureConnected()) {
            _errorFlow.tryEmit(context.getString(com.indus.veena.R.string.player_disconnected))
            return
        }

        withContext(Dispatchers.Main) {
            try {
                val mediaItem = MediaItem.Builder()
                    .setMediaId("${song.provider}:${song.id}:$requestId")
                    .setUri(url)
                    .setRequestMetadata(MediaItem.RequestMetadata.Builder().setExtras(android.os.Bundle().apply {
                        putBundle("stream_headers", android.os.Bundle().apply {
                            song.playbackHeaders.forEach { (name, value) -> putString(name, value) }
                        })
                    }).build())
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

                    _isPlaying.value = controller.isPlaying
                    _playbackState.value = controller.playbackState
                    artworkJob = artworkScope.launch {
                        try {
                            val artwork = context.getCachedArtworkBlob(song.thumbnail) ?: return@launch
                            if (selectionId == requestId && controller.currentMediaItem?.mediaId == mediaItem.mediaId) {
                                controller.replaceMediaItem(0, mediaItem.buildUpon().setMediaMetadata(
                                    mediaItem.mediaMetadata.buildUpon()
                                        .setArtworkData(artwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER).build()
                                ).build())
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Log.w("MusicController", "Artwork unavailable for ${song.id}", e)
                        }
                    }

                    // Force play if the service is in a weird state
                    if (!controller.playWhenReady) {
                        controller.playWhenReady = true
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errorFlow.tryEmit(context.getString(com.indus.veena.R.string.song_load_failed, e.message.orEmpty()))
            }
        }
    }

    /** Replace an expired transport while retaining media identity and cached notification artwork. */
    suspend fun retryStream(url: String, song: SongModel, position: Long) {
        if (!ensureConnected()) return
        withContext(Dispatchers.Main) {
            val controller = checkNotNull(mediaController) { context.getString(com.indus.veena.R.string.player_disconnected) }
            val item = checkNotNull(controller.currentMediaItem) { context.getString(com.indus.veena.R.string.player_disconnected) }
            val artworkChanged = item.mediaMetadata.artworkUri?.toString().orEmpty() != song.thumbnail
            val metadata = when {
                artworkChanged -> MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist)
                    .setArtworkUri(song.thumbnail.toUri()).build()
                item.mediaMetadata.title?.toString() != song.title || item.mediaMetadata.artist?.toString() != song.artist ->
                    item.mediaMetadata.buildUpon().setTitle(song.title).setArtist(song.artist).build()
                else -> item.mediaMetadata
            }
            val refreshed = item.buildUpon().setUri(url).setMediaMetadata(metadata)
                .setRequestMetadata(MediaItem.RequestMetadata.Builder().setExtras(android.os.Bundle().apply {
                    putBundle("stream_headers", android.os.Bundle().apply {
                        song.playbackHeaders.forEach { (name, value) -> putString(name, value) }
                    })
                }).build()).build()
            // An older artwork job must not replace this item with the expired URI it captured.
            artworkJob?.cancel()
            controller.setMediaItem(refreshed, position.coerceAtLeast(0L))
            controller.prepare()
            controller.play()
            if (artworkChanged || metadata.artworkData == null) {
                val expectedSelection = selectionId
                artworkJob = artworkScope.launch {
                    try {
                        val bytes = context.getCachedArtworkBlob(song.thumbnail) ?: return@launch
                        val current = controller.currentMediaItem
                        if (selectionId == expectedSelection && current?.mediaId == item.mediaId) {
                            controller.replaceMediaItem(0, current.buildUpon().setMediaMetadata(
                                current.mediaMetadata.buildUpon().setArtworkData(bytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER).build()
                            ).build())
                        }
                    } catch (e: CancellationException) { throw e }
                    catch (e: Exception) { Log.w("MusicController", "Retry artwork unavailable", e) }
                }
            }
        }
    }

    fun togglePlayPause() {
        if (mediaController?.isConnected == true) {
            mediaController?.let { controller ->
                if (controller.playWhenReady && controller.playbackState != Player.STATE_ENDED) {
                    controller.pause()
                } else {
                    if (controller.playbackState == Player.STATE_ENDED) controller.seekToDefaultPosition()
                    controller.play()
                }
            }
        }
    }

    fun seekTo(position: Long) {
        if (mediaController?.isConnected == true) {
            mediaController?.seekTo(position)
        }
    }

    fun pauseForTrackChange() {
        selectionId++
        artworkJob?.cancel()
        mediaController?.takeIf { it.isConnected && it.isPlaying }?.pause()
    }

    fun stopPlayer() {
        selectionId++
        artworkJob?.cancel()
        if (mediaController?.isConnected == true) {
            mediaController?.stop()
            mediaController?.clearMediaItems()
        }
    }
}