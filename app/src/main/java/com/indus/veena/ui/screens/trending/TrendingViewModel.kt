package com.indus.veena.ui.screens.trending

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indus.veena.database.DataStoreKeys
import com.indus.veena.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TrendingState(
    val providers: List<MusicRepository.ProviderItem> = emptyList(),
    val provider: String = "",
    val sections: List<MusicRepository.DiscoverySection> = emptyList(),
    val loading: Boolean = false,
    val query: String = "",
    val error: String? = null,
    val loadingMore: Boolean = false,
    val hasMore: Boolean = false
)

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val preferences: DataStore<Preferences>
) : ViewModel() {
    private val _state = MutableStateFlow(TrendingState())
    val state = _state.asStateFlow()
    private var discoveryJob: Job? = null
    private var searchPage = 1
    private val cache = mutableMapOf<String, List<MusicRepository.DiscoverySection>>()

    init {
        viewModelScope.launch {
            repository.awaitExtensionsLoaded()
            val saved = preferences.data.first()[DataStoreKeys.DISCOVERY_PROVIDER]
            var restored = false
            repository.discoveryProviders.collect { providers ->
                cache.keys.retainAll(providers.map { it.id }.toSet())
                _state.update { it.copy(providers = providers) }
                if (!restored) {
                    restored = true
                    // An explicitly saved empty ID means None, including across app restarts.
                    selectProvider(if (saved == null) providers.firstOrNull()?.id.orEmpty()
                        else saved.takeIf { id -> providers.any { it.id == id } }.orEmpty())
                } else if (_state.value.provider.isNotBlank() && providers.none { it.id == _state.value.provider }) {
                    selectProvider("")
                }
            }
        }
    }

    fun selectProvider(id: String) {
        viewModelScope.launch { preferences.edit { it[DataStoreKeys.DISCOVERY_PROVIDER] = id } }
        _state.update { it.copy(provider = id, query = "") }
        load(refresh = false)
    }
    fun search(query: String) {
        _state.update { it.copy(query = query.trim()) }
        load(refresh = false)
    }
    fun refresh() = load(refresh = true)

    fun loadMore() {
        val current = _state.value
        if (current.query.isBlank() || current.loading || current.loadingMore || !current.hasMore) return
        _state.update { it.copy(loadingMore = true, error = null) }
        discoveryJob = viewModelScope.launch {
            try {
                val next = repository.searchDiscovery(current.provider, current.query, searchPage + 1)
                ensureActive()
                var added = false
                val merged = current.sections.map { section ->
                    val extra = next.firstOrNull { it.id == section.id }
                    val songs = (section.songs + extra?.songs.orEmpty()).distinctBy { it.provider to it.id }
                    val collections = (section.collections + extra?.collections.orEmpty()).distinctBy { it.kind to it.id }
                    if (songs.size > section.songs.size || collections.size > section.collections.size) added = true
                    section.copy(songs = songs, collections = collections)
                } + next.filter { candidate -> current.sections.none { it.id == candidate.id } }.also { if (it.isNotEmpty()) added = true }
                searchPage++
                _state.update { it.copy(sections = merged, loadingMore = false, hasMore = added) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { ensureActive(); _state.update { it.copy(loadingMore = false, error = e.localizedMessage.orEmpty()) } }
        }
    }

    private fun load(refresh: Boolean) {
        discoveryJob?.cancel()
        searchPage = 1
        val provider = _state.value.provider
        val query = _state.value.query
        if (provider.isBlank()) {
            _state.update { it.copy(sections = emptyList(), loading = false, error = null) }
            return
        }
        val cached = if (!refresh && query.isBlank()) cache[provider] else null
        _state.update { it.copy(sections = cached ?: if (refresh) it.sections else emptyList(),
            loading = cached == null, error = null, loadingMore = false, hasMore = false) }
        if (cached != null) return
        discoveryJob = viewModelScope.launch {
            try {
                val sections = if (query.isBlank()) repository.discover(provider)
                    else repository.searchDiscovery(provider, query)
                ensureActive()
                if (query.isBlank()) cache[provider] = sections
                _state.update { it.copy(sections = sections, loading = false, hasMore = query.isNotBlank() && sections.isNotEmpty()) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { ensureActive(); _state.update { it.copy(loading = false, error = e.localizedMessage.orEmpty()) } }
        }
    }
}
