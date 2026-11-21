package com.indus.veena.ui.screens.trending

import com.indus.veena.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indus.veena.ui.components.SongArtwork
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.models.PlaybackQueue
import com.indus.veena.ui.screens.home.SongListItem
import com.indus.veena.ui.screens.player.PlayerState

@Composable
fun DiscoverySongScreen(
    paddingValues: PaddingValues,
    playerState: PlayerState,
    onBack: () -> Unit,
    originQueue: PlaybackQueue?,
    onPlay: (PlaybackQueue) -> Unit,
    viewModel: DiscoverySongViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val trendingSource = stringResource(R.string.queue_trending)
    val matchesSource = stringResource(R.string.search)
    val song = state.song
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) { viewModel.events.collect { snackbar.showSnackbar(it) } }
    val download = downloads[song.id]
    val accent = MaterialTheme.colorScheme.primary
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.about_this_song)) }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
            })
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            contentPadding = PaddingValues(
                start = 20.dp, end = 20.dp,
                bottom = paddingValues.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "song", contentType = "song-hero") {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    val colors = MaterialTheme.colorScheme
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        colors.primaryContainer,
                                        colors.surface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        SongArtwork(
                            model = song.thumbnail, contentDescription = song.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(24.dp)
                                .widthIn(max = 320.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(22.dp))
                        )
                    }
                    Text(
                        song.extensionName.ifBlank { song.provider }.uppercase(),
                        style = MaterialTheme.typography.labelLarge, color = colors.primary
                    )
                    Text(
                        song.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            song.album, song.year, song.genre,
                            song.duration.toLongOrNull()?.takeIf { it > 0 }
                                ?.let { "${it / 60}:${(it % 60).toString().padStart(2, '0')}" }
                                .orEmpty()
                        )
                            .filter { it.isNotBlank() }.forEach { label ->
                                SuggestionChip(onClick = {}, label = { Text(label) })
                            }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { onPlay(originQueue ?: PlaybackQueue.from(trendingSource, listOf(song), song)) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text(
                            stringResource(R.string.play)
                        )
                        }
                        FilledTonalButton(
                            onClick = {
                                if (download?.state == DownloadState.PAUSED) viewModel.toggleDownload(
                                    song.id,
                                    false
                                )
                                else viewModel.download()
                            }, modifier = Modifier.weight(1f), enabled = !state.preparingDownload &&
                                    (download == null || download.state in listOf(
                                        DownloadState.FAILED,
                                        DownloadState.PAUSED
                                    ))
                        ) {
                            Icon(Icons.Default.Download, null); Spacer(Modifier.width(6.dp))
                            Text(
                                when {
                                    state.preparingDownload -> stringResource(R.string.preparing)
                                    download?.state == DownloadState.COMPLETED -> stringResource(R.string.downloaded)
                                    download?.state == DownloadState.PAUSED -> stringResource(R.string.resume)
                                    download != null && download.state != DownloadState.FAILED -> stringResource(R.string.download_progress_percent, download.progress)
                                    else -> stringResource(R.string.download)
                                }
                            )
                        }
                    }
                    if (song.composer.isNotBlank()) Text(
                        stringResource(R.string.composed_by, song.composer),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!song.lyricist.isNullOrBlank()) Text(
                        stringResource(R.string.lyrics_by_name, song.lyricist.orEmpty()),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    state.error?.let { message ->
                        Text(message, color = colors.error)
                        TextButton(onClick = viewModel::loadDetails) { Text(stringResource(R.string.retry_details)) }
                    }
                    OutlinedButton(
                        onClick = viewModel::findOtherProviders,
                        enabled = state.matches.none { it.loading },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Search,
                            null
                        ); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.find_on_other_providers))
                    }
                    if (state.searched && state.matches.isEmpty()) Text(stringResource(R.string.install_another_addon_to_compare_providers))
                }
            }
            state.matches.forEach { group ->
                item(key = "provider:${group.id}", contentType = "provider-heading") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            group.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (group.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        else if (group.songs.isEmpty()) Text(
                            stringResource(R.string.no_matching_songs_found),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(
                    group.songs,
                    key = { "match:${group.id}:${it.id}" },
                    contentType = { "song-row" }) { result ->
                    val entry = downloads[result.id]
                    SongListItem(
                        song = result, onClick = { onPlay(PlaybackQueue.from(matchesSource, group.songs, result)) },
                        onTogglePause = {
                            viewModel.toggleDownload(
                                result.id,
                                entry?.state != DownloadState.PAUSED
                            )
                        },
                        onDownloadClick = { viewModel.download(result) }, accentColor = accent,
                        isNowPlaying = playerState.activeSong?.let { it.id == result.id && it.provider == result.provider } == true,
                        isPlaying = playerState.isPlaying, downloadEntity = entry)
                }
            }
        }
    }
}
