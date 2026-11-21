package com.indus.veena.ui.screens.trending

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.di.DownloadManager
import com.indus.veena.models.SongModel
import com.indus.veena.navigation.Screen
import com.indus.veena.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class ProviderMatches(
    val id: String,
    val name: String,
    val songs: List<SongModel> = emptyList(),
    val loading: Boolean = true
)

data class DiscoverySongState(
    val song: SongModel,
    val loading: Boolean = true,
    val error: String? = null,
    val matches: List<ProviderMatches> = emptyList(),
    val searched: Boolean = false,
    val preparingDownload: Boolean = false
)

@HiltViewModel
class DiscoverySongViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository,
    private val downloadsManager: DownloadManager,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    private val initialSong =
        Json.decodeFromString<SongModel>(savedStateHandle.toRoute<Screen.DiscoverySong>().song)
    private val _state = MutableStateFlow(DiscoverySongState(initialSong))
    val state = _state.asStateFlow()
    val downloads = downloadsManager.activeDownloads.map { list -> list.associateBy { it.songId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var searchJob: Job? = null

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val details = repository.getSongDetails(initialSong.id, initialSong.provider)
                _state.update {
                    it.copy(
                        song = details?.mergeWith(initialSong) ?: initialSong, loading = false,
                        error = if (details == null) context.getString(com.indus.veena.R.string.addon_removed) else null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loading = false,
                        error = e.message ?: context.getString(com.indus.veena.R.string.could_not_load_details)
                    )
                }
            }

        }
    }

    fun findOtherProviders() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val song = _state.value.song
            val providers = repository.availableProviders.first().filter { it.id != song.provider }
            _state.update {
                it.copy(
                    searched = true,
                    matches = providers.map { p -> ProviderMatches(p.id, p.name) })
            }
            supervisorScope {
                providers.map { provider ->
                    launch {
                        val results =
                            repository.searchSongs("${song.title} ${song.artist}", provider.id)
                        ensureActive()
                        _state.update { state ->
                            state.copy(matches = state.matches.map {
                                if (it.id == provider.id) it.copy(songs = results.distinctBy { s -> s.id }
                                    .take(20), loading = false) else it
                            })
                        }
                    }
                }.joinAll()
            }
        }
    }

    fun download(song: SongModel = _state.value.song) {
        if (_state.value.preparingDownload) return
        val existing = downloads.value[song.id]
        if (existing != null && existing.state !in listOf(
                DownloadState.FAILED,
                DownloadState.PAUSED
            )
        ) return
        _state.update { it.copy(preparingDownload = true) }
        viewModelScope.launch {
            try {
                val details = repository.getSongDetails(song.id, song.provider)?.mergeWith(song)
                    ?: error(context.getString(com.indus.veena.R.string.could_not_resolve_song))
                val quality = dataStore.data.first()[DataStoreKeys.DOWNLOAD_QUALITY_KEY]
                val urls = details.streamableUrls
                val url = when (quality) {
                    "LOW" -> urls["48kbps"] ?: urls["96kbps"] ?: urls.values.lastOrNull()
                    "MEDIUM" -> urls["128kbps"] ?: urls["160kbps"] ?: urls.values.firstOrNull()
                    else -> urls["320kbps"] ?: urls["256kbps"] ?: urls["160kbps"]
                    ?: urls.values.firstOrNull()
                }?.takeIf { it.isNotBlank() } ?: error(context.getString(com.indus.veena.R.string.no_downloadable_stream))
                downloadsManager.startDownload(details, url)
                _events.send(context.getString(com.indus.veena.R.string.download_started, details.title))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(e.message ?: context.getString(com.indus.veena.R.string.download_failed))
            } finally {
                _state.update { it.copy(preparingDownload = false) }
            }
        }
    }

    fun toggleDownload(id: String, downloading: Boolean) {
        viewModelScope.launch {
            if (downloading) downloadsManager.pauseDownload(id) else downloadsManager.resumeDownload(
                id
            )
        }
    }
}

/** Some stream resolvers return only URLs; retain the discovery metadata. */
private fun SongModel.mergeWith(original: SongModel) = copy(
    title = title.ifBlank { original.title },
    artist = artist.ifBlank { original.artist },
    thumbnail = thumbnail.ifBlank { original.thumbnail },
    album = album.ifBlank { original.album },
    duration = duration.takeUnless { it == "0" || it.isBlank() } ?: original.duration,
    composer = composer.ifBlank { original.composer },
    genre = genre.ifBlank { original.genre },
    year = year.ifBlank { original.year },
    lyricist = lyricist?.ifBlank { original.lyricist.orEmpty() } ?: original.lyricist
)
