package com.indus.veena.ui.screens.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.media3.common.Player
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.database.sqlite.daos.FavouriteDao
import com.indus.veena.database.sqlite.entities.FavouriteEntity
import com.indus.veena.di.DataStoreModule.getSnapshot
import com.indus.veena.di.DownloadManager
import com.indus.veena.helpers.VeenaLog
import com.indus.veena.models.Provider
import com.indus.veena.models.SongModel
import com.indus.veena.models.retainUnchangedPresentation
import com.indus.veena.repository.MusicRepository
import com.indus.veena.service.MusicController
import com.indus.veena.R
import com.indus.veena.models.PlaybackQueue
import com.indus.veena.service.QueueNavigation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@Stable
data class PlayerState(
    val queue: PlaybackQueue? = null,
    val activeSong: SongModel? = null,
    val isPlaying: Boolean = false,
    val isFavourite: Boolean = false,
    val duration: Long = 1L,
    val bufferState: Boolean = false,
    val dominantColor: Color = Color.White,
    val displayMode: PlayerDisplayMode = PlayerDisplayMode.HIDDEN
)

enum class PlayerDisplayMode {
    HIDDEN, MINI, FULL
}

@Singleton
class PlaybackCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicController: MusicController,
    queueNavigation: QueueNavigation,
    private val repository: MusicRepository,
    private val downloadManager: DownloadManager,
    private val dataStore: DataStore<Preferences>,
    private val favouriteDao: FavouriteDao
) {
    private val playbackScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val TAG = "PLAYER_VM"

    private val _uiState = MutableStateFlow(PlayerState())
    val uiState = _uiState.asStateFlow()
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent = _toastEvent.asSharedFlow()
    private val _queueEndEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val queueEndEvent = _queueEndEvent.asSharedFlow()

    val activeDownloads = downloadManager.activeDownloads.stateIn(
        playbackScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeFavouriteStatus() {
        playbackScope.launch {
            _uiState.map { it.activeSong?.id }.distinctUntilChanged().flatMapLatest { id ->
                id?.let { favouriteDao.isFavourite(it) } ?: flowOf(false)
            }.collect { isFav ->
                _uiState.update { it.copy(isFavourite = isFav) }
            }
        }
    }

    private var silentRetry = false
    private var retriedStream = false
    private var playJob: Job? = null
    private var resolvingSong = false
    private var seeking = false
    private var resolutionFailed = false

    init {
        observeFavouriteStatus()
        playbackScope.launch {
            queueNavigation.steps.collect { moveQueue(it) }
        }
        playbackScope.launch {
            musicController.errorFlow.collect { errorMsg ->
                val active = _uiState.value.activeSong
                if (!resolvingSong && active != null && active.provider != Provider.LOCAL.name && !retriedStream &&
                    (com.indus.veena.service.StreamFailurePolicy.shouldRefresh(
                        com.indus.veena.service.StreamFailurePolicy.httpStatus(Exception(errorMsg))))) {
                    retriedStream = true
                    playSong(active, refreshStream = true, resumePosition = _currentPosition.value) { message -> _toastEvent.tryEmit(message) }
                } else {
                    silentRetry = false
                    Log.e("PlayerVM", "Error received: $errorMsg")
                    _uiState.update { it.copy(bufferState = false) }
                    _toastEvent.emit(errorMsg)
                }
            }
        }

        playbackScope.launch {
            musicController.isPlaying.collect { isPlaying ->
                _uiState.update { it.copy(isPlaying = !resolvingSong && (isPlaying || (seeking && it.isPlaying))) }
            }
        }

        playbackScope.launch {
            musicController.playbackState.collect { playbackState ->
                if (playbackState == Player.STATE_ENDED && !resolvingSong &&
                    musicController.mediaController?.playbackState == Player.STATE_ENDED
                ) {
                    moveQueue(1)
                    return@collect
                }
                if (playbackState == Player.STATE_READY) { seeking = false; silentRetry = false }
                _uiState.update {
                    it.copy(bufferState = !silentRetry && (resolvingSong || (!seeking && playbackState == Player.STATE_BUFFERING)))
                }
            }
        }

        playbackScope.launch {
            musicController.currentDuration.collect { duration ->
                _uiState.update { it.copy(duration = if (resolvingSong || duration <= 0L) it.duration else duration) }
            }
        }

        playbackScope.launch {
            musicController.closeEvent.collect {
                stopPlayer()
            }
        }

        playbackScope.launch {
            uiState.map { it.activeSong != null }.distinctUntilChanged().collectLatest { active ->
                if (!active) return@collectLatest
                while (true) {
                    val controller = musicController.mediaController
                    if (!resolvingSong && controller != null && controller.isConnected) {
                        _currentPosition.value = controller.currentPosition.coerceAtLeast(0L)
                        if (controller.playbackState == Player.STATE_READY) seeking = false
                        _uiState.update { it.copy(
                            isPlaying = controller.isPlaying || (seeking && it.isPlaying),
                            bufferState = !silentRetry && !seeking && controller.playbackState == Player.STATE_BUFFERING,
                            duration = controller.duration.takeIf { duration -> duration > 0L } ?: it.duration
                        ) }
                    }
                    delay(250)
                }
            }
        }
    }

    fun playQueue(queue: PlaybackQueue) {
        selectTrack(queue)
    }

    fun selectQueueTrack(index: Int) {
        val queue = _uiState.value.queue ?: return
        if (index in queue.songs.indices && index != queue.index) {
            selectTrack(queue.copy(index = index))
        }
    }

    fun moveQueue(step: Int) {
        val queue = _uiState.value.queue ?: return
        val index = queue.index + step
        if (index >= queue.songs.size) {
            _queueEndEvent.tryEmit(Unit)
        } else {
            selectQueueTrack(index)
        }
    }

    private fun selectTrack(queue: PlaybackQueue) {
        val song = queue.songs[queue.index]
        playSong(song, queue = queue) { _toastEvent.tryEmit(it) }
    }

    private fun playSong(song: SongModel, refreshStream: Boolean = false, resumePosition: Long = 0L, queue: PlaybackQueue? = null, onFailure: ((String) -> Unit)) {
        if (!refreshStream) retriedStream = false
        silentRetry = refreshStream
        playJob?.cancel()
        seeking = false
        resolvingSong = true
        resolutionFailed = false
        _currentPosition.value = resumePosition
        if (refreshStream) {
            // A new signed URL is transport state, not a new song selection.
            _uiState.update { it.copy(bufferState = false) }
        } else {
            _uiState.update { it.copy(queue = queue, activeSong = song, isPlaying = false, duration = 1L,
                bufferState = true, isFavourite = it.isFavourite && it.activeSong?.id == song.id, dominantColor = Color(0xFF202838)) }
            musicController.pauseForTrackChange()
        }
        playJob = playbackScope.launch {
            VeenaLog.d(TAG, "STAGE 1: Request -> [${song.title}] from [${song.provider}]")


            try {
                if (song.provider == Provider.LOCAL.name) {
                    VeenaLog.d(TAG, "STAGE 2: Detected LOCAL song. Playing from path: ${song.url}")
                    val uri = when {
                        song.url.startsWith("content://") ->
                            song.url

                        song.url.startsWith("file://") ->
                            song.url

                        else ->
                            Uri.fromFile(File(song.url)).toString()
                    }
                    resolvingSong = false
                    musicController.playSong(uri, song)
                    VeenaLog.d(TAG, "STAGE 3: Local Playback Started.")
                    return@launch
                }

                VeenaLog.d(TAG, "STAGE 2: Fetching full metadata for ID: ${song.id}")
                val detailedSong = repository.getSongDetails(song.id, song.provider, forceRefresh = refreshStream)?.let { detail ->
                    detail.copy(title = detail.title.ifBlank { song.title }, artist = detail.artist.ifBlank { song.artist },
                        thumbnail = detail.thumbnail.ifBlank { song.thumbnail }, album = detail.album.ifBlank { song.album })
                }
                coroutineContext.ensureActive()
                _uiState.update { current ->
                    val resolved = detailedSong ?: song
                    val display = if (refreshStream) resolved.retainUnchangedPresentation(current.activeSong ?: song) else resolved
                    if (display === current.activeSong) current else current.copy(activeSong = display)
                }
                val qualityName = dataStore.getSnapshot(DataStoreKeys.AUDIO_QUALITY_KEY, DataStoreKeys.AudioQuality.HIGH.name)
                val quality = try { DataStoreKeys.AudioQuality.valueOf(qualityName) } catch(e:Exception) { DataStoreKeys.AudioQuality.HIGH }
                VeenaLog.d(TAG, "STAGE 3: Selecting Stream (User Pref: $quality)")
                val streamUrl = detailedSong?.streamableUrls?.let { selectBestStream(it, quality) }

                if (streamUrl != null) {
                    VeenaLog.d(TAG, "STAGE 4: Passing URI (Quality-$quality) to Media3 Service -> $streamUrl")
                    resolvingSong = false
                    if (refreshStream) musicController.retryStream(streamUrl, detailedSong, resumePosition)
                    else musicController.playSong(streamUrl, detailedSong)
                    VeenaLog.d(TAG, "STAGE 5: Controller Hand-off Complete.")
                } else {
                    resolvingSong = false
                    silentRetry = false
                    resolutionFailed = true
                    val msg = context.getString(com.indus.veena.R.string.no_stream_for_song, song.title)
                    Log.e("PlayerVM", msg)
                    VeenaLog.e(TAG, "ERROR @ STAGE 4: URL Selection failed. Result was null.")
                    _uiState.update { it.copy(bufferState = false) }
                    onFailure(msg)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                resolvingSong = false
                silentRetry = false
                resolutionFailed = true
                Log.e("PlayerVM", "Playback failed", e)
                VeenaLog.e(TAG, "CRITICAL ERROR @ STAGE 2/3: Exception caught in lifecycle", e)
                _uiState.update { it.copy(bufferState = false) }
                onFailure(context.getString(R.string.song_load_failed, e.localizedMessage.orEmpty()))
            }
        }
    }

    fun togglePlayPause() {
        if (resolvingSong) return
        val queue = uiState.value.queue ?: return
        val controller = musicController.mediaController
        if (resolutionFailed || controller?.currentMediaItem == null || controller.playerError != null) {
            selectTrack(queue)
        } else {
            musicController.togglePlayPause()
        }
    }
    fun seekTo(pos: Long) {
        if (_uiState.value.bufferState) return
        val position = pos.coerceIn(0L, _uiState.value.duration)
        seeking = true
        _currentPosition.value = position
        musicController.seekTo(position)
    }
    fun setDisplayMode(mode: PlayerDisplayMode) {
        _uiState.update { it.copy(displayMode = mode) }
    }

    fun updateDominantColor(color: Color) {
        val hex = String.format("#%08X", color.toArgb())
        VeenaLog.d(TAG, "Dominant Color Hex: $hex")
        _uiState.update { it.copy(dominantColor = color) }
    }

    fun stopPlayer() {
        playJob?.cancel()
        resolvingSong = false
        seeking = false
        _currentPosition.value = 0L
        musicController.stopPlayer()
        _uiState.update { PlayerState() } // Reset everything
    }

    private fun selectBestStream(urls: Map<String, String>, preferred: DataStoreKeys.AudioQuality): String? {
        if (urls.isEmpty()) return null

        // Extract numeric bitrates for comparison: "128kbps" -> 128
        val sortedStreams = urls.mapNotNull { entry ->
            val bps = entry.key.replace("kbps", "").toIntOrNull() ?: 0
            bps to entry.value
        }.sortedByDescending { it.first }

        return when (preferred) {
            DataStoreKeys.AudioQuality.HIGH -> sortedStreams.first().second
            DataStoreKeys.AudioQuality.LOW -> sortedStreams.last().second
            DataStoreKeys.AudioQuality.MEDIUM -> {
                // Try to find something around 128-160, otherwise middle of the list
                val target = sortedStreams.find { it.first in 128..160 }
                target?.second ?: sortedStreams[sortedStreams.size / 2].second
            }
        }
    }

    fun downloadCurrentSong() {
        val song = _uiState.value.activeSong ?: return
        playbackScope.launch {
            try {
                // We already have the detailed metadata and streamableUrls
                // from the playSong() lifecycle!
                val detailedSong = repository.getSongDetails(song.id, song.provider)

                // Re-select URL using the same logic as playback
                val qualityName = dataStore.getSnapshot(DataStoreKeys.DOWNLOAD_QUALITY_KEY, DataStoreKeys.AudioQuality.HIGH.name)
                val quality = try { DataStoreKeys.AudioQuality.valueOf(qualityName) } catch(e:Exception) { DataStoreKeys.AudioQuality.HIGH }

                val urls = detailedSong?.streamableUrls
                val streamUrl = urls?.let { selectBestStream(it, quality) }

                if (streamUrl != null) {
                    // Use the existing SongModel (which is already rich with data)
                    downloadManager.startDownload(
                        song = song.copy(
                            streamableUrls = detailedSong.streamableUrls,
                            downloadHeaders = detailedSong.downloadHeaders),
                        streamUrl = streamUrl,
                        artworkData = null // Worker will fetch/cache if needed
                    )
                } else {
                    _toastEvent.emit(context.getString(com.indus.veena.R.string.no_downloadable_stream))
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                VeenaLog.e(TAG, "Download trigger failed", e)
                _toastEvent.emit(context.getString(com.indus.veena.R.string.download_failed_reason, e.message ?: context.getString(com.indus.veena.R.string.could_not_resolve_stream)))
            }
        }
    }

    fun toggleFavourite() {
        val song = _uiState.value.activeSong ?: return
        val isFav = _uiState.value.isFavourite
        playbackScope.launch {
            try {
                if (isFav) {
                    favouriteDao.deleteBySongId(song.id)
                } else {
                    favouriteDao.insertFavourite(
                        FavouriteEntity(
                            songId = song.id,
                            title = song.title,
                            artist = song.artist,
                            thumbnail = song.thumbnail,
                            url = song.url,
                            duration = song.duration,
                            provider = song.provider,
                            extensionName = song.extensionName,
                            album = song.album,
                            year = song.year,
                            composer = song.composer,
                            genre = song.genre
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Could not update favourite", e)
                _toastEvent.tryEmit(context.getString(R.string.queue_favourite_failed))
            }
        }
    }
}