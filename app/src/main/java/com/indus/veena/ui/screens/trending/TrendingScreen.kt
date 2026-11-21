@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.indus.veena.ui.screens.trending

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import com.indus.veena.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indus.veena.ui.components.SongArtwork
import com.indus.veena.models.PlaybackQueue
import com.indus.veena.models.SongModel

@Composable
fun TrendingScreen(
    paddingValues: PaddingValues,
    onSongClick: (PlaybackQueue) -> Unit,
    onAddonsClick: () -> Unit,
    onCollectionClick: (com.indus.veena.repository.MusicRepository.CatalogItem) -> Unit,
    accentColor: androidx.compose.ui.graphics.Color? = null,
    viewModel: TrendingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val queueSource = stringResource(if (state.query.isBlank()) R.string.queue_trending else R.string.search)
    var expanded by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scrollingUp = remember { mutableStateOf(true) }
    val fabScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && kotlin.math.abs(available.y) > 1f) {
                    scrollingUp.value = available.y > 0f
                }
                return Offset.Zero
            }
        }
    }
    val fabExpanded by remember { derivedStateOf { scrollingUp.value || !listState.canScrollBackward } }
    var searchVisible by rememberSaveable(state.provider) { mutableStateOf(false) }
    var query by rememberSaveable(state.provider) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val submitSearch = {
        focusManager.clearFocus()
        keyboard?.hide()
        viewModel.search(query)
    }
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val animationsActive = lifecycleState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
    LaunchedEffect(searchVisible) {
        if (searchVisible) {
            listState.animateScrollToItem(0)

        }
    }
    val accent = accentColor?.takeIf {
        it != androidx.compose.ui.graphics.Color.Unspecified && it != androidx.compose.ui.graphics.Color.Transparent
    } ?: MaterialTheme.colorScheme.primary
    val providerName = state.providers.firstOrNull { it.id == state.provider }?.name.orEmpty()
    val chooseSourceLabel = stringResource(R.string.choose_a_source)
    val selectedSourceLabel = providerName.ifBlank { stringResource(R.string.none) }
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(modifier = Modifier.drawBehind {
                com.indus.veena.ui.screens.home.drawLampGlow(this, accent)
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent), title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    com.indus.veena.ui.screens.home.VeenaTitle(accent,
                        isAnimActive = animationsActive, modifier = Modifier.padding(vertical = 8.dp))
                    Surface(modifier = Modifier.offset(y = 2.dp), shape = RoundedCornerShape(8.dp), color = accent.copy(alpha = 0.16f)) {
                        Text(stringResource(R.string.discover), color = accent, style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }, actions = {
                IconButton(onClick = { searchVisible = !searchVisible }, enabled = state.provider.isNotBlank()) {
                    Icon(Icons.Default.Search, stringResource(R.string.search_provider))
                }
                IconButton(
                    onClick = viewModel::refresh,
                    enabled = !state.loading && state.provider.isNotBlank()
                ) {
                    if (state.loading) LoadingIndicator(Modifier.size(28.dp))
                    else Icon(Icons.Default.Refresh, stringResource(R.string.refresh_discovery))
                }
            })
        },
        floatingActionButton = {
            Column(
                Modifier.padding(bottom = (paddingValues.calculateBottomPadding() - 8.dp).coerceAtLeast(0.dp)),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(expanded) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (state.providers.isEmpty()) {
                                TextButton(onClick = onAddonsClick) { Text(stringResource(R.string.get_discovery_addons)) }
                            }
                            TextButton(onClick = { expanded = false; viewModel.selectProvider("") }) {
                                Icon(if (state.provider.isBlank()) Icons.Default.Check else Icons.Default.Block, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.none))
                            }
                            state.providers.forEach { provider ->
                                TextButton(onClick = {
                                    expanded = false; viewModel.selectProvider(
                                    provider.id
                                )
                                }) {
                                    Icon(
                                        if (provider.id == state.provider) Icons.Default.Check else Icons.Default.Extension,
                                        null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(provider.name)
                                }
                            }
                        }
                    }
                }
                ExtendedFloatingActionButton(
                    modifier = Modifier.semantics {
                        contentDescription = chooseSourceLabel
                        stateDescription = selectedSourceLabel
                    },
                    onClick = { expanded = !expanded },
                    expanded = expanded || fabExpanded,
                    icon = {
                        Icon(
                            if (expanded) Icons.Default.Close else Icons.Default.Explore,
                            null
                        )
                    },
                    text = { Text(if (expanded) stringResource(R.string.choose_a_source) else providerName.ifBlank { stringResource(R.string.sources) }) })
            }
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .nestedScroll(fabScrollConnection)
                .padding(top = innerPadding.calculateTopPadding()),
            state = listState,
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 96.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (searchVisible) item(key = "search", contentType = "search") {
                com.indus.veena.ui.screens.home.SearchBar(
                    query = query, onQueryChange = { query = it }, onSearch = submitSearch,
                    active = true, onActiveChange = { if (!it) { searchVisible = false; viewModel.search("") } },
                    onClearClick = { query = ""; viewModel.search("") }, borderColor = accent
                ) { }

            }
            item(key = "intro", contentType = "intro") {
                val colors = MaterialTheme.colorScheme
                Column(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    colors.primaryContainer,
                                    colors.secondaryContainer
                                )
                            )
                        )
                        .padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        stringResource(R.string.your_next_favorite), style = MaterialTheme.typography.labelMedium,
                        color = colors.onPrimaryContainer
                    )
                    Text(
                        stringResource(R.string.a_world_of_music_one_place_to_explore),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimaryContainer
                    )
                    Text(
                        if (providerName.isBlank()) stringResource(R.string.choose_a_music_source_to_get_started) else stringResource(R.string.fresh_picks, providerName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onPrimaryContainer
                    )
                }
            }
            if (state.loading && state.sections.isEmpty()) item(key = "loading", contentType = "status") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoadingIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.finding_music_for_you))
                }
            }
            if (state.providers.isEmpty()) item(key = "no-providers", contentType = "status") {
                DiscoveryMessage(
                    stringResource(R.string.more_music_starts_here),
                    stringResource(R.string.install_or_update_an_addon_that_supports_discovery),
                    stringResource(R.string.manage_addons),
                    onAddonsClick
                )
            }
            state.error?.let { error ->
                item(key = "error", contentType = "status") {
                    DiscoveryMessage(
                        stringResource(R.string.couldn_t_load_this_source),
                        error,
                        stringResource(R.string.try_again),
                        viewModel::refresh
                    )
                }
            }
            if (!state.loading && state.provider.isNotBlank() && state.sections.isEmpty() && state.error == null) {
                item(key = "empty", contentType = "status") {
                    DiscoveryMessage(
                        stringResource(if (state.query.isBlank()) R.string.no_picks else R.string.no_results),
                        stringResource(R.string.try_refreshing_or_choose_another_source),
                        stringResource(R.string.refresh),
                        viewModel::refresh
                    )
                }
            }
            items(state.sections, key = { "section:${state.provider}:${it.id}" }, contentType = { "music-section" }) { section ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            section.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (section.subtitle.isNotBlank()) Text(
                            section.subtitle, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (section.collections.isNotEmpty()) LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(section.collections, key = { "${it.kind}:${it.id}" }, contentType = { it.kind }) { item ->
                            Column(Modifier.width(164.dp)
                                .clickable { onCollectionClick(item) }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SongArtwork(model = item.thumbnail, contentDescription = null, contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(164.dp).clip(if (item.kind == "artist")
                                        androidx.compose.foundation.shape.CircleShape else RoundedCornerShape(18.dp)))
                                Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                                Text(stringResource(if (item.kind == "artist") R.string.artist else R.string.album),
                                    color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                                if (item.subtitle.isNotBlank()) Text(item.subtitle, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (section.songs.isNotEmpty()) LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            section.songs,
                            key = { "${it.provider}:${it.id}" },
                            contentType = { "discovery-song" }) { song ->
                            DiscoveryCard(song, onClick = { onSongClick(PlaybackQueue.from(queueSource, state.sections.flatMap { it.songs }, song)) })
                        }
                    }
                }
            }
            if (state.hasMore) item(key = "more-results") {
                TextButton(onClick = viewModel::loadMore, enabled = !state.loadingMore,
                    modifier = Modifier.padding(horizontal = 20.dp)) {
                    if (state.loadingMore) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text(stringResource(R.string.load_more))
                }
            }
        }
    }
}

@Composable
private fun DiscoveryCard(song: SongModel, onClick: () -> Unit) {
    Column(
        Modifier
            .width(164.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SongArtwork(
            model = song.thumbnail, contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(164.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Text(
            song.title, maxLines = 2, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold
        )
        Text(
            song.artist,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DiscoveryMessage(title: String, message: String, action: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FilledTonalButton(onClick = onClick) { Text(action) }
    }
}
