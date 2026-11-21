@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.indus.veena.ui.screens.trending

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.toRoute
import com.indus.veena.R
import com.indus.veena.models.PlaybackQueue
import com.indus.veena.navigation.Screen
import com.indus.veena.repository.MusicRepository
import com.indus.veena.ui.components.SongArtwork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CollectionState(
    val page: MusicRepository.CollectionPage? = null, val loading: Boolean = true,
    val error: String? = null, val loadingMore: Boolean = false, val moreError: String? = null
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository
) : ViewModel() {
    val route = savedStateHandle.toRoute<Screen.Collection>()
    private val _state = MutableStateFlow(CollectionState())
    val state = _state.asStateFlow()
    private var job: Job? = null
    private var nextPage = 0

    init {
        reload()
    }

    fun reload() {
        job?.cancel()
        job = viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val page = repository.collection(route.provider, route.id, route.kind)
                nextPage = 0
                val first = if (route.kind == "artist") repository.artistSongs(
                    route.provider,
                    route.id,
                    0
                ) else null
                if (first != null) nextPage = 1
                _state.value = CollectionState(
                    if (first != null) page.copy(
                        songs = first.songs,
                        hasMore = first.hasMore
                    ) else page, loading = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive(); _state.update {
                    it.copy(
                        loading = false,
                        error = e.localizedMessage.orEmpty()
                    )
                }
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        val page = current.page ?: return
        if (!page.hasMore || current.loadingMore) return
        _state.update { it.copy(loadingMore = true, moreError = null) }
        job = viewModelScope.launch {
            try {
                val result =
                    repository.artistSongs(route.provider, route.id, nextPage) ?: return@launch
                val songs = (page.songs + result.songs).distinctBy { it.provider to it.id }
                nextPage++
                _state.update {
                    it.copy(
                        page = page.copy(
                            songs = songs,
                            hasMore = result.hasMore && songs.size > page.songs.size
                        ), loadingMore = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ensureActive(); _state.update {
                    it.copy(
                        loadingMore = false,
                        moreError = e.localizedMessage.orEmpty()
                    )
                }
            }
        }
    }

}

@Composable
fun CollectionScreen(
    paddingValues: PaddingValues, onBack: () -> Unit, onSongClick: (PlaybackQueue) -> Unit,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val page = state.page
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(if (viewModel.route.kind == "artist") R.string.artist else R.string.album)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        stringResource(R.string.back)
                    )
                }
            })
    }) { inner ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = inner.calculateTopPadding()),
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header", contentType = "collection-header") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        SongArtwork(model = page?.thumbnail?.ifBlank { viewModel.route.thumbnail }
                            ?: viewModel.route.thumbnail,
                            contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .widthIn(max = 320.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(28.dp)))
                    }
                    Text(page?.title?.ifBlank { viewModel.route.title } ?: viewModel.route.title,
                        style = MaterialTheme.typography.headlineLarge)
                    page?.subtitle?.takeIf { it.isNotBlank() }
                        ?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                    page?.description?.takeIf { it.isNotBlank() }
                        ?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    if (state.loading) LoadingIndicator(Modifier.align(Alignment.CenterHorizontally))
                    state.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::reload) { Text(stringResource(R.string.try_again)) }
                    }
                    if (!state.loading && state.error == null && page?.songs.isNullOrEmpty()) Text(
                        stringResource(R.string.collection_empty)
                    )
                }
            }
            items(
                page?.songs.orEmpty(),
                key = { "${it.provider}:${it.id}" },
                contentType = { "collection-song" }) { song ->
                ListItem(
                    headlineContent = { Text(song.title, maxLines = 2) },
                    supportingContent = { Text(song.artist, maxLines = 1) },
                    leadingContent = {
                        SongArtwork(
                            model = song.thumbnail,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSongClick(PlaybackQueue.from(page?.title.orEmpty().ifBlank { viewModel.route.title }, page?.songs.orEmpty(), song)) })
            }
            if (page?.hasMore == true) item(key = "more") {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    state.moreError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = viewModel::loadMore, enabled = !state.loadingMore) {
                        if (state.loadingMore) CircularProgressIndicator(
                            Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        else Text(stringResource(R.string.load_more))
                    }
                }
            }
        }
    }
}
