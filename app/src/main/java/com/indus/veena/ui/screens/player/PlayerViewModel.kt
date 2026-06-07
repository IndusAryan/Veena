package com.indus.veena.ui.screens.player

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.di.DataStoreModule.getSnapshot
import com.indus.veena.di.DownloadManager
import com.indus.veena.helpers.VeenaLog
import com.indus.veena.models.Provider
import com.indus.veena.models.SongModel
import com.indus.veena.repository.MusicRepository
import com.indus.veena.service.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicController: MusicController,
    private val repository: MusicRepository,
    private val downloadManager: DownloadManager,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {
    val TAG = "PLAYER_VM"
    enum class PlayerDisplayMode {
        HIDDEN, MINI, FULL
    }

    data class PlayerState(
        val activeSong: SongModel? = null,
        val isPlaying: Boolean = false,
        val duration: Long = 1L,
        val bufferState: Boolean = false,
        val dominantColor: Color = Color.White,
        val errorMessage: String? = null,
        val displayMode: PlayerDisplayMode = PlayerDisplayMode.HIDDEN
    )

    private val _uiState = MutableStateFlow(PlayerState())
    val uiState = _uiState.asStateFlow()
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    val activeDownloads = downloadManager.activeDownloads.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            musicController.errorFlow.collect { errorMsg ->
                Log.e("PlayerVM", "Error received: $errorMsg")
                showToast(errorMsg)
                _uiState.update { it.copy(bufferState = false, errorMessage = errorMsg) }
            }
        }

        viewModelScope.launch {
            musicController.isPlaying.collect { isPlaying ->
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
        }

        viewModelScope.launch {
            musicController.playbackState.collect { playbackState ->
                _uiState.update {
                    it.copy(bufferState = playbackState == Player.STATE_BUFFERING)
                }
            }
        }

        viewModelScope.launch {
            musicController.currentDuration.collect { duration ->
                _uiState.update { it.copy(duration = duration.coerceAtLeast(1L)) }
            }
        }

        viewModelScope.launch {
            while (true) {
                val controller = musicController.mediaController
                if (controller != null && controller.isConnected && controller.isPlaying) {
                    _currentPosition.value = controller.currentPosition
                }
                delay(1000)
            }
        }
    }

    fun playSong(song: SongModel) {
        viewModelScope.launch {
            VeenaLog.d(TAG, "STAGE 1: Request -> [${song.title}] from [${song.provider}]")
            _uiState.update { it.copy(activeSong = song, bufferState = true) }

            if (song.provider == Provider.LOCAL.name) {
                VeenaLog.d(TAG, "STAGE 2: Detected LOCAL song. Playing from path: ${song.url}")
                val uri = Uri.fromFile(File(song.url)).toString()
                musicController.playSong(uri, song)
                VeenaLog.d(TAG, "STAGE 3: Local Playback Started.")
                return@launch
            }

            try {
                VeenaLog.d(TAG, "STAGE 2: Fetching full metadata for ID: ${song.id}")
                val detailedSong = repository.getSongDetails(song.id, song.provider)
                val qualityName = dataStore.getSnapshot(DataStoreKeys.AUDIO_QUALITY_KEY, DataStoreKeys.AudioQuality.HIGH.name)
                val quality = try { DataStoreKeys.AudioQuality.valueOf(qualityName) } catch(e:Exception) { DataStoreKeys.AudioQuality.HIGH }
                VeenaLog.d(TAG, "STAGE 3: Selecting Stream (User Pref: $quality)")
                val streamUrl = if (song.provider == Provider.LOCAL.name) {
                    Uri.fromFile(File(song.url)).toString()
                } else {
                    val qualityStr = dataStore.getSnapshot(DataStoreKeys.AUDIO_QUALITY_KEY, "HIGH")
                    val quality = DataStoreKeys.AudioQuality.valueOf(qualityStr)
                    detailedSong?.streamableUrls?.let { selectBestStream(it, quality) }
                }

                if (streamUrl != null) {
                    VeenaLog.d(TAG, "STAGE 4: Passing URI (Quality-$quality) to Media3 Service -> $streamUrl")
                    musicController.playSong(streamUrl, song)
                    VeenaLog.d(TAG, "STAGE 5: Controller Hand-off Complete.")
                } else {
                    Log.e("PlayerVM", "No streamable URL found for ${song.title}")
                    VeenaLog.e(TAG, "ERROR @ STAGE 4: URL Selection failed. Result was null.")
                }
            } catch (e: Exception) {
                Log.e("PlayerVM", "Playback failed", e)
                showToast("Failed to load song: ${e.message}")
                VeenaLog.e(TAG, "CRITICAL ERROR @ STAGE 2/3: Exception caught in lifecycle", e)
                _uiState.update { it.copy(bufferState = false, errorMessage = e.message) }
            }
        }
    }

    fun togglePlayPause() = musicController.togglePlayPause()
    fun seekTo(pos: Long) {
        _currentPosition.value = pos
        musicController.seekTo(pos)
    }
    fun setDisplayMode(mode: PlayerDisplayMode) {
        _uiState.update { it.copy(displayMode = mode) }
    }

    fun updateDominantColor(color: Color) {
        VeenaLog.d(TAG, "Dominant Color: $color")
        _uiState.update { it.copy(dominantColor = color) }
    }

    fun stopPlayer() {
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
        viewModelScope.launch {
            try {
                // We already have the detailed metadata and streamableUrls
                // from the playSong() lifecycle!
                val detailedSong = repository.getSongDetails(song.id, song.provider)

                // Re-select URL using the same logic as playback
                val qualityName = dataStore.getSnapshot(DataStoreKeys.AUDIO_QUALITY_KEY, DataStoreKeys.AudioQuality.HIGH.name)
                val quality = try { DataStoreKeys.AudioQuality.valueOf(qualityName) } catch(e:Exception) { DataStoreKeys.AudioQuality.HIGH }

                val urls = detailedSong?.streamableUrls
                val streamUrl = when (quality) {
                    DataStoreKeys.AudioQuality.HIGH -> urls?.get("320kbps") ?: urls?.values?.firstOrNull()
                    DataStoreKeys.AudioQuality.MEDIUM -> urls?.get("128kbps") ?: urls?.values?.firstOrNull()
                    DataStoreKeys.AudioQuality.LOW -> urls?.get("48kbps") ?: urls?.values?.lastOrNull()
                }

                if (streamUrl != null) {
                    // Use the existing SongModel (which is already rich with data)
                    downloadManager.startDownload(
                        song = song,
                        streamUrl = streamUrl,
                        artworkData = null // Worker will fetch/cache if needed
                    )
                }
            } catch (e: Exception) {
                VeenaLog.e(TAG, "Download trigger failed", e)
            }
        }
    }

    private fun showToast(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(musicController.ctx, message, Toast.LENGTH_LONG).show()
        }
    }
}