package com.indus.veena.ui.screens.home

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.indus.veena.R
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.models.SongModel
import com.indus.veena.repository.MusicRepository
import com.indus.veena.ui.screens.player.PlayerState

@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    onSongClick: (SongModel) -> Unit,
    onAddonsClick: () -> Unit,
    playerState: PlayerState,
    playerDominantColor: Color? = Color.Unspecified,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle(emptyList())
    val providerNames by viewModel.availableProviders.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val state = uiState as? HomeUiState.Ready ?: return

    val defaultPrimary = MaterialTheme.colorScheme.primary
    val safeAccentColor = remember(playerDominantColor, defaultPrimary) {
        playerDominantColor?.takeIf {
            it != Color.Unspecified && it != Color.Transparent && it != Color(0xFF1A1616)
        } ?: defaultPrimary
    }

    LaunchedEffect(state.uiState.errorMessage) {
        state.uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    HomeScreenContent(
        paddingValues = paddingValues,
        state = state.uiState,
        suggestions = suggestions,
        searchHistory = searchHistory,
        downloads = downloads,
        providerNames = providerNames,
        safeAccentColor = safeAccentColor,
        playerState = playerState,
        onSongClick = onSongClick,
        onAddonsClick = onAddonsClick,
        onDownloadSong = { song -> viewModel.downloadSong(song, context) },
        onTogglePause = viewModel::togglePause,
        onQueryChange = viewModel::onQueryChange,
        onSearchTriggered = viewModel::onSearchTriggered,
        onDeleteHistory = viewModel::deleteHistoryItem,
        onClearHistory = viewModel::clearAllHistory,
        onProviderSelected = viewModel::onProviderSelected,
        onClearSearch = viewModel::onClearSearch
    )
}

@Composable
private fun HomeScreenContent(
    paddingValues: PaddingValues,
    state: HomeContentState,
    suggestions: List<String>,
    searchHistory: List<String>,
    downloads: List<DownloadEntity>,
    providerNames: List<MusicRepository.ProviderItem>,
    safeAccentColor: Color,
    playerState: PlayerState,
    onSongClick: (SongModel) -> Unit,
    onAddonsClick: () -> Unit,
    onDownloadSong: (SongModel) -> Unit,
    onTogglePause: (String, Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onProviderSelected: (MusicRepository.ProviderItem) -> Unit,
    onClearSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isSearchBarActive by remember { mutableStateOf(false) }
    var topBarHeight by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()

    // Observe lifecycle for animation pausing
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    BackHandler(enabled = isSearchBarActive) {
        isSearchBarActive = false
        focusManager.clearFocus()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (state.isLoading) {
            ShimmerSongList(
                topOffset = with(LocalDensity.current) { topBarHeight.toDp() },
                bottomPadding = paddingValues.calculateBottomPadding(),
                isAnimActive = isResumed
            )
        } else if (state.isProvidersEmpty) {
            EmptySourcesState(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                onAddonsClick = onAddonsClick
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    top = with(LocalDensity.current) { topBarHeight.toDp() } + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 80.dp,
                    start = 12.dp,
                    end = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.searchResults, key = { it.id }) { song ->
                    val downloadState = downloads.find { it.songId == song.id }
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        downloadEntity = downloadState,
                        isNowPlaying = song.id == playerState.activeSong?.id,
                        isPlaying = playerState.isPlaying,
                        accentColor = safeAccentColor,
                        isAnimActive = isResumed,
                        onDownloadClick = { onDownloadSong(song) },
                        onTogglePause = {
                            onTogglePause(song.id, downloadState?.state == DownloadState.DOWNLOADING)
                        }
                    )
                }
            }
        }

        VeenaTopAppBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onGloballyPositioned { topBarHeight = it.size.height },
            state = state,
            history = searchHistory,
            safeAccentColor = safeAccentColor,
            suggestions = suggestions,
            isSearchBarActive = isSearchBarActive,
            isAnimActive = isResumed,
            onActiveChange = { isSearchBarActive = it },
            onQueryChange = onQueryChange,
            availableProviders = providerNames,
            onSearchTrigger = {
                isSearchBarActive = false
                focusManager.clearFocus()
                onSearchTriggered()
            },
            onHistoryClick = { query ->
                onQueryChange(query)
                isSearchBarActive = false
                focusManager.clearFocus()
                onSearchTriggered()
            },
            onDeleteHistory = onDeleteHistory,
            onClearHistory = onClearHistory,
            onClearClick = onClearSearch,
            onProviderClick = onProviderSelected
        )
    }
}

@Composable
private fun EmptySourcesState(
    modifier: Modifier = Modifier,
    onAddonsClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No sources configured",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Install addons to start searching and downloading music",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.Button(
            onClick = onAddonsClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Install Addons")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VeenaTopAppBar(
    modifier: Modifier = Modifier,
    state: HomeContentState,
    history: List<String>,
    suggestions: List<String>,
    availableProviders: List<MusicRepository.ProviderItem>,
    isSearchBarActive: Boolean,
    safeAccentColor: Color,
    isAnimActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchTrigger: () -> Unit,
    onHistoryClick: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onProviderClick: (MusicRepository.ProviderItem) -> Unit,
    onClearClick: () -> Unit,
) {
    val animatedHeaderColor by animateColorAsState(
        targetValue = safeAccentColor,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "headerColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "borderSweep")
    val sweepOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    var startExpansion by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startExpansion = true }

    val expansion by animateFloatAsState(
        targetValue = if (startExpansion) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "lampExpansion"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .drawWithContent {
                drawContent()
                val borderY = size.height - 1.5.dp.toPx()
                val borderHeight = 1.5.dp.toPx()
                val glowHeight = 18.dp.toPx()
                val baseAlpha = 0.45f * expansion

                drawRect(
                    color = animatedHeaderColor.copy(alpha = baseAlpha),
                    topLeft = Offset(0f, borderY),
                    size = Size(size.width, borderHeight)
                )

                if (isAnimActive) {
                    val highlightWidth = size.width * 0.35f
                    val highlightCenter = sweepOffset * (size.width + highlightWidth) - highlightWidth / 2f
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, animatedHeaderColor.copy(alpha = 0.9f), Color.Transparent),
                            startX = highlightCenter - highlightWidth / 2f,
                            endX = highlightCenter + highlightWidth / 2f
                        ),
                        topLeft = Offset(0f, borderY),
                        size = Size(size.width, borderHeight)
                    )
                }

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(animatedHeaderColor.copy(alpha = baseAlpha * 0.4f), Color.Transparent),
                        startY = borderY,
                        endY = borderY + glowHeight
                    ),
                    topLeft = Offset(0f, borderY),
                    size = Size(size.width, glowHeight)
                )
            }
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp).align(Alignment.TopCenter)) {
            drawLampGlow(drawScope = this, color = animatedHeaderColor, expansion = expansion)
        }

        Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(bottom = 12.dp)) {
            AnimatedVisibility(
                visible = !isSearchBarActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                VeenaTitle(accentColor = animatedHeaderColor, isAnimActive = isAnimActive)
            }
            SearchBar(
                query = state.searchQuery,
                onQueryChange = onQueryChange,
                onSearch = onSearchTrigger,
                active = isSearchBarActive,
                onActiveChange = onActiveChange,
                onClearClick = onClearClick,
                borderColor = animatedHeaderColor
            ) {
                SearchContent(
                    query = state.searchQuery,
                    history = history,
                    suggestions = suggestions,
                    onHistoryClick = onHistoryClick,
                    onDeleteHistory = onDeleteHistory,
                    onClearHistory = onClearHistory
                )
            }

            AnimatedVisibility(visible = !isSearchBarActive) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableProviders) { provider ->
                        FilterChip(
                            selected = state.selectedProvider == provider.id,
                            onClick = { onProviderClick(provider) },
                            label = { Text(provider.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                selectedContainerColor = animatedHeaderColor.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun drawLampGlow(drawScope: DrawScope, color: Color, expansion: Float = 1f) {
    with(drawScope) {
        val center = Offset(x = size.width / 2f, y = 0f)
        val radius = size.height * 1.35f
        scale(scaleX = 1.2f * expansion, scaleY = 1.0f * expansion, pivot = center) {
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to color.copy(alpha = 0.55f * expansion),
                        0.35f to color.copy(alpha = 0.25f * expansion),
                        0.65f to color.copy(alpha = 0.08f * expansion),
                        1.0f to Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onClearClick: () -> Unit,
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val glassBrush = remember(borderColor) {
        Brush.linearGradient(
            colors = listOf(
                borderColor.copy(alpha = 0.6f),
                borderColor.copy(alpha = 0.2f),
                borderColor.copy(alpha = 0.5f)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(width = 1.2.dp, brush = glassBrush, shape = RoundedCornerShape(14.dp))
            .animateContentSize(animationSpec = tween(300, easing = FastOutSlowInEasing))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    enabled = !active
                ) {
                    onActiveChange(true)
                    focusRequester.requestFocus()
                }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (active) {
                IconButton(
                    onClick = {
                        onActiveChange(false)
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused && !active) onActiveChange(true)
                    },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch()
                    focusManager.clearFocus()
                }),
                cursorBrush = SolidColor(borderColor),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search songs...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onClearClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        }

        AnimatedVisibility(
            visible = active,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) { content() }
        }
    }
}

@Composable
private fun VeenaTitle(accentColor: Color, isAnimActive: Boolean) {
    val transition = rememberInfiniteTransition(label = "vTitle")
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimActive) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleShimmer"
    )

    val onSurface = MaterialTheme.colorScheme.onSurface
    val textBrush = remember(accentColor, shimmerOffset) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0f to onSurface,
                (shimmerOffset - 0.15f).coerceAtLeast(0f) to onSurface,
                shimmerOffset to accentColor,
                (shimmerOffset + 0.15f).coerceAtMost(1f) to onSurface,
                1f to onSurface
            )
        )
    }

    Text(
        text = "Veena",
        style = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = FontFamily(Font(R.font.playfair_display_variable)),
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic,
            fontSize = 32.sp,
            brush = textBrush
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun ShimmerSongList(topOffset: androidx.compose.ui.unit.Dp, bottomPadding: androidx.compose.ui.unit.Dp, isAnimActive: Boolean) {
    val alpha by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0.25f,
        targetValue = if (isAnimActive) 0.65f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    LazyColumn(
        contentPadding = PaddingValues(top = topOffset + 16.dp, bottom = bottomPadding + 80.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false
    ) {
        items(7) { ShimmerSongItemPlaceholder(alphaProvider = { alpha }) }
    }
}

@Composable
private fun ShimmerSongItemPlaceholder(alphaProvider: () -> Float) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .graphicsLayer { alpha = alphaProvider() }
            .background(surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(onSurface))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.65f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(onSurface))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(11.dp).clip(RoundedCornerShape(4.dp)).background(onSurface.copy(alpha = 0.7f)))
        }
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(50)).background(onSurface.copy(alpha = 0.6f)))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongListItem(
    song: SongModel,
    onClick: () -> Unit,
    onTogglePause: () -> Unit,
    onDownloadClick: () -> Unit,
    accentColor: Color,
    isNowPlaying: Boolean = false,
    isPlaying: Boolean = false,
    isAnimActive: Boolean = true,
    downloadEntity: DownloadEntity?
) {
    val pulseAlpha by rememberInfiniteTransition(label = "rowPulse").animateFloat(
        initialValue = 0.0f,
        targetValue = if (isAnimActive) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .drawWithContent {
                drawContent()
                if (isNowPlaying) {
                    val currentPulse = if (isPlaying) pulseAlpha * 0.25f else 0.12f
                    val brush = Brush.horizontalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.25f + currentPulse),
                            accentColor.copy(alpha = 0.75f + currentPulse),
                            accentColor.copy(alpha = 0.25f + currentPulse),
                        )
                    )
                    drawRoundRect(
                        brush = brush,
                        style = Stroke(width = 1.5.dp.toPx()),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
            }
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = song.thumbnail,
            contentDescription = null,
            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = song.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        if (isNowPlaying) {
            NowPlayingBars(isPlaying = isPlaying, isAnimActive = isAnimActive, color = accentColor, modifier = Modifier.padding(end = 4.dp))
        }

        AnimatedContent(
            targetState = when {
                downloadEntity?.state == DownloadState.FETCHING || downloadEntity?.state == DownloadState.PENDING -> "BUSY_INDETERMINATE"
                downloadEntity?.state == DownloadState.COMPLETED -> "COMPLETED"
                downloadEntity?.state == DownloadState.FAILED -> "IDLE"
                downloadEntity != null -> "PROGRESS"
                else -> "IDLE"
            },
            transitionSpec = {
                scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() togetherWith
                        scaleOut(spring(dampingRatio = Spring.DampingRatioNoBouncy)) + fadeOut()
            },
            label = "DownloadState"
        ) { state ->
            when (state) {
                "BUSY_INDETERMINATE" -> CircularProgressIndicator(modifier = Modifier.padding(12.dp).size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                "COMPLETED" -> IconButton(onClick = { }) { Icon(Icons.Rounded.CheckCircle, contentDescription = "Downloaded", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp)) }
                "PROGRESS" -> {
                    val progressValue = (downloadEntity?.progress ?: 0) / 100f
                    val isProcessing = progressValue >= 0.99f && downloadEntity?.state != DownloadState.COMPLETED

                    Box(contentAlignment = Alignment.Center) {
                        val animatedProgress by animateFloatAsState(targetValue = progressValue, animationSpec = if (progressValue == 0f) snap() else tween(500), label = "progress")

                        if (isProcessing) {
                            CircularWavyProgressIndicator(modifier = Modifier.size(46.dp), stroke = Stroke(3f), trackColor = MaterialTheme.colorScheme.surfaceVariant, color = MaterialTheme.colorScheme.primary)
                        } else {
                            CircularWavyProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(46.dp), stroke = Stroke(3f), trackColor = MaterialTheme.colorScheme.surfaceVariant, color = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = onTogglePause, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (downloadEntity?.state == DownloadState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Pause/Resume",
                                modifier = Modifier.size(18.dp),
                                tint = if (isProcessing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                else -> IconButton(onClick = onDownloadClick) { Icon(Icons.Default.Download, contentDescription = "Download") }
            }
        }
    }
}

@Composable
fun NowPlayingBars(isPlaying: Boolean, isAnimActive: Boolean, modifier: Modifier = Modifier, color: Color = Color.White) {
    val infiniteTransition = rememberInfiniteTransition(label = "bars")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = if (isAnimActive) 1f else 0.3f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = if (isAnimActive) 1f else 0.6f,
        animationSpec = infiniteRepeatable(tween(450, delayMillis = 150, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = if (isAnimActive) 1f else 0.2f,
        animationSpec = infiniteRepeatable(tween(520, delayMillis = 80, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b3"
    )

    val fractions = if (isPlaying && isAnimActive) listOf(bar1, bar2, bar3) else listOf(0.35f, 0.55f, 0.3f)
    val animatedFractions = fractions.mapIndexed { i, target ->
        val v by animateFloatAsState(target, tween(180), label = "settle$i")
        v
    }

    val maxHeight = 16.dp
    val barWidth = 3.dp

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        animatedFractions.forEach { fraction ->
            Spacer(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight()
                    .drawBehind {
                        val barHeight = size.height * fraction
                        drawRoundRect(
                            color = color.copy(alpha = 0.85f),
                            topLeft = Offset(0f, size.height - barHeight),
                            size = Size(size.width, barHeight),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                    }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchContent(
    query: String,
    history: List<String>,
    suggestions: List<String>,
    onHistoryClick: (String) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear Search History") },
            text = { Text("Are you sure you want to clear all search history? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearHistory()
                    showClearConfirm = false
                }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (query.isEmpty() && history.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { showClearConfirm = true }) {
                    Text("Clear All", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                history.forEach { searchTerm ->
                    AssistChip(
                        onClick = { onHistoryClick(searchTerm) },
                        label = { Text(searchTerm) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { onDeleteHistory(searchTerm) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        if (query.length >= 3 && suggestions.isNotEmpty()) {
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onHistoryClick(suggestion) },
                        label = { Text(suggestion) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        if (query.isEmpty() && history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Search for songs, artists, or albums",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}