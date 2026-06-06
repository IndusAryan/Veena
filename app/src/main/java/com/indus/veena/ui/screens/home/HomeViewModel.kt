package com.indus.veena.ui.screens.home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.database.sqlite.daos.SearchHistoryDao
import com.indus.veena.database.sqlite.entities.SearchHistoryEntity
import com.indus.veena.di.DataStoreModule.getSnapshot
import com.indus.veena.di.DownloadManager
import com.indus.veena.helpers.ImageModuleCoil.getCachedArtworkBlob
import com.indus.veena.helpers.VeenaLog
import com.indus.veena.lifecycle.ioScope
import com.indus.veena.models.SongModel
import com.indus.veena.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Ready(val uiState: HomeContentState) : HomeUiState
}

data class HomeContentState(
    val searchQuery: String = "",
    val searchResults: List<SongModel> = emptyList(),
    val isSearchActive: Boolean = false,
    val activeSongId: String = "",
    val selectedProvider: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val dataStore: DataStore<Preferences>,
    private val downloadManager: DownloadManager,
    private val searchHistoryDao: SearchHistoryDao,
) : ViewModel() {
    private val TAG = "HomeViewModel"

    private val _uiState = MutableStateFlow<HomeUiState>(
        HomeUiState.Ready(HomeContentState())
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions = _suggestions

    val availableProviders = repository.availableProviders.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val searchHistory: StateFlow<List<String>> = searchHistoryDao.getHistoryQueries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null
    private var suggestionJob: Job? = null

    init {
        ioScope {
            availableProviders.collect { providers ->
                val currentState = (_uiState.value as? HomeUiState.Ready)?.uiState ?: return@collect
                if (currentState.selectedProvider.isEmpty() && providers.isNotEmpty()) {
                    updateContentState { it.copy(selectedProvider = providers.first().id) }
                }
            }
        }
    }

    val downloads = downloadManager.activeDownloads.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun downloadSong(song: SongModel, context: Context) {
        viewModelScope.launch {
            downloadManager.prepareDownloadEntry(song)
            VeenaLog.d(TAG, "Download Request -> [${song.title}] from [${song.provider}]")
            try {
                VeenaLog.d(TAG, "Fetching full metadata for ID: ${song.id}")
                val detailedSong = repository.getSongDetails(song.id, song.provider)
                val urls = detailedSong?.streamableUrls
                val qualityName = dataStore.getSnapshot(
                    DataStoreKeys.DOWNLOAD_QUALITY_KEY,
                    DataStoreKeys.AudioQuality.HIGH.name
                )
                val quality = try {
                    DataStoreKeys.AudioQuality.valueOf(qualityName)
                } catch(e: Exception) {
                    DataStoreKeys.AudioQuality.HIGH
                }
                VeenaLog.d(TAG, "Selecting Download Stream (User Pref: $quality)")
                val streamUrl = if (urls != null) {
                    when (quality) {
                        DataStoreKeys.AudioQuality.HIGH -> urls["320kbps"] ?: urls["256kbps"] ?: urls["128kbps"] ?: urls.values.firstOrNull()
                        DataStoreKeys.AudioQuality.MEDIUM -> urls["128kbps"] ?: urls["320kbps"] ?: urls.values.firstOrNull()
                        DataStoreKeys.AudioQuality.LOW -> urls["48kbps"] ?: urls["96kbps"] ?: urls.values.lastOrNull()
                    }
                } else null

                // 4. Start Download
                if (streamUrl != null) {
                    val cachedBlob = context.getCachedArtworkBlob(song.thumbnail)
                    VeenaLog.d(TAG, "Passing URI to DownloadManager -> $streamUrl")
                    val mergedSong = song.copy(
                        title = detailedSong?.title?.takeIf { it.isNotBlank() } ?: song.title,
                        artist = detailedSong?.artist?.takeIf { it.isNotBlank() } ?: song.artist,
                        album = detailedSong?.album?.takeIf { it.isNotBlank() } ?: song.album ,
                        composer = detailedSong?.composer?.takeIf { it.isNotBlank() } ?: song.composer,
                        genre = detailedSong?.genre?.takeIf { it.isNotBlank() } ?: song.genre,
                        year = detailedSong?.year?.takeIf { it.isNotBlank() } ?: song.year,
                        thumbnail = detailedSong?.thumbnail?.takeIf { it.isNotBlank() } ?: song.thumbnail
                    )
                    downloadManager.startDownload(
                        song = mergedSong,
                        streamUrl = streamUrl,
                        artworkData = cachedBlob, // if cached already
                        customHeaders = /*mapOf("Range" to "bytes=0-") */ emptyMap()
                    )
                    VeenaLog.d(TAG, "Download Enqueued Successfully.")
                } else {
                    VeenaLog.e(TAG, "No streamable URL found for download: ${song.title}")
                    VeenaLog.e(TAG, "ERROR: Download URL Selection failed. Result was null.")
                }
            } catch (e: Exception) {
                VeenaLog.e(TAG, "Failed to prepare download", e)
                VeenaLog.e(TAG, "CRITICAL ERROR: Exception caught while preparing download", e)
                downloadManager.removeDownload(song.id)
            }
        }
    }

    fun togglePause(songId: String, isCurrentlyDownloading: Boolean) {
        ioScope {
            if (isCurrentlyDownloading) downloadManager.pauseDownload(songId)
            else downloadManager.resumeDownload(songId)
        }
    }

    fun onProviderSelected(provider: MusicRepository.ProviderItem) {
        updateContentState { it.copy(selectedProvider = provider.id) }
        val currentState = (_uiState.value as? HomeUiState.Ready)?.uiState ?: return
        if (currentState.searchQuery.isNotBlank()) {
            performSearch(currentState.searchQuery, provider.id)
        }
    }

    fun onSearchTriggered() {
        val currentState = (_uiState.value as? HomeUiState.Ready)?.uiState ?: return
        if (currentState.searchQuery.isNotBlank()) {
            performSearch(currentState.searchQuery, currentState.selectedProvider)
            addSearchToHistory(currentState.searchQuery)
        }
    }

    fun onQueryChange(newQuery: String) {
        updateContentState { it.copy(searchQuery = newQuery) }
        suggestionJob?.cancel()
        if (newQuery.isNotBlank()) {
            suggestionJob = viewModelScope.launch(Dispatchers.IO) {
                delay(300)
                val results = repository.getSuggestions(newQuery)
                _suggestions.value = results
            }
        } else {
            _suggestions.value = emptyList()
        }
    }

    private fun addSearchToHistory(query: String) {
        ioScope {
            searchHistoryDao.insertHistory(SearchHistoryEntity(query = query))
        }
    }

    fun deleteHistoryItem(query: String) {
        ioScope {
          searchHistoryDao.deleteHistoryItem(query)
        }
    }

    fun clearAllHistory() {
        ioScope {
            searchHistoryDao.clearHistory()
        }
    }

    fun onClearSearch() {
        searchJob?.cancel()
        updateContentState {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                isSearchActive = false,
                isLoading = false
            )
        }
    }

    private fun performSearch(query: String, provider: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            updateContentState { it.copy(isLoading = true, isSearchActive = true, errorMessage = null) }
            try {
                val results = repository.searchSongs(query, provider)
                updateContentState {
                    it.copy(searchResults = results, isLoading = false)
                }
            } catch (_: Exception) {
                updateContentState {
                    it.copy(isLoading = false, errorMessage = "Failed to load results. Please check your connection.")
                }
            }
        }
    }

    private fun updateContentState(transform: (HomeContentState) -> HomeContentState) {
        _uiState.update { currentState ->
            if (currentState is HomeUiState.Ready) {
                HomeUiState.Ready(transform(currentState.uiState))
            } else {
                HomeUiState.Ready(transform(HomeContentState()))
            }
        }
    }

    fun clearError() {
        updateContentState { it.copy(errorMessage = null) }
    }
}