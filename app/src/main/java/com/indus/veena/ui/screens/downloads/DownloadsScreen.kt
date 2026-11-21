package com.indus.veena.ui.screens.downloads

import com.indus.veena.ui.components.localizedLabel
import com.indus.veena.R
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.models.Provider
import com.indus.veena.models.PlaybackQueue
import com.indus.veena.models.SongModel
import com.indus.veena.ui.screens.favourites.FavouritesViewModel
import com.indus.veena.ui.screens.favourites.toPlaybackSong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onSongClick: (PlaybackQueue) -> Unit,
    onShowAllFavourites: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel(),
    favViewModel: FavouritesViewModel = hiltViewModel()
) {
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val favourites by favViewModel.favourites.collectAsStateWithLifecycle(emptyList())

    val downloadsSource = stringResource(R.string.downloads)
    val favouritesSource = stringResource(R.string.favourites)

    var songToDelete by remember { mutableStateOf<String?>(null) }

    if (songToDelete != null) {
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text(stringResource(R.string.delete_download)) },
            text = { Text(stringResource(R.string.this_will_delete_the_history_from_here_but_your_file_on_disk_will)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        songToDelete?.let { viewModel.removeDownload(it) }
                        songToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library), fontWeight = FontWeight.Bold) },
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 32.dp, // Above mini player!
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Favourites Section
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.favourites),
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onShowAllFavourites) {
                            Text(stringResource(R.string.show_all))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (favourites.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.no_favourites_yet), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        // Horizontal scroll for a few favourites
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            favourites.take(5).forEach { fav ->
                                Box(modifier = Modifier.width(280.dp)) {
                                    DownloadItemCard(
                                        entity = DownloadEntity(
                                            songId = fav.songId,
                                            title = fav.title,
                                            artist = fav.artist,
                                            artworkUrl = fav.thumbnail,
                                            url = fav.url,
                                            extensionName = fav.extensionName,
                                            customHeaders = "",
                                            state = DownloadState.COMPLETED
                                        ),
                                        onCardClick = {
                                            onSongClick(PlaybackQueue.from(favouritesSource, favourites.map { it.toPlaybackSong() }, fav.toPlaybackSong()))
                                        },
                                        onTogglePause = {},
                                        onCancel = { favViewModel.removeFavourite(fav.songId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    stringResource(R.string.downloads),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (downloads.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Download, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.no_active_downloads), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(downloads, key = { it.songId }) { entity ->
                DownloadItemCard(
                    entity = entity,
                    onCardClick = {
                        if (entity.state == DownloadState.COMPLETED) {
                            val songs = downloads.filter { it.state == DownloadState.COMPLETED }
                                .map { it.toPlaybackSong() }
                            onSongClick(PlaybackQueue.from(downloadsSource, songs, entity.toPlaybackSong()))
                        }
                    },
                    onTogglePause = { viewModel.togglePause(entity.songId, entity.state == DownloadState.DOWNLOADING) },
                    onCancel = { songToDelete = entity.songId }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DownloadItemCard(
    entity: DownloadEntity,
    onCardClick: () -> Unit,
    onTogglePause: () -> Unit,
    onCancel: () -> Unit
) {
    val isFinished = entity.state == DownloadState.COMPLETED
    val progress by animateFloatAsState(targetValue = entity.progress / 100f, label = "progress")
    val mbDownloaded = "%.1f".format(entity.downloadedBytes / (1024f * 1024f))
    val mbTotal = "%.1f".format(entity.totalBytes / (1024f * 1024f))
    val containerColor = MaterialTheme.colorScheme.surfaceContainer

    Card(
        modifier = Modifier.fillMaxWidth().height(128.dp),
        shape = RoundedCornerShape(14.dp),
        onClick = { if (entity.state == DownloadState.COMPLETED) onCardClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = entity.artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(30.dp, BlurredEdgeTreatment.Unbounded)
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)))
            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = entity.artworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
                )

                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entity.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entity.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = entity.extensionName.ifBlank { entity.comment.removePrefix("Veena: ") },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (!isFinished) {
                        Text(
                            text = stringResource(R.string.download_size_status, entity.state.localizedLabel(), mbDownloaded, mbTotal),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        when (entity.state) {
                            DownloadState.COMPLETED -> Icon(
                                Icons.Rounded.CheckCircle, stringResource(R.string.downloaded),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            DownloadState.FETCHING, DownloadState.PENDING -> CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            else -> {
                                if (entity.state == DownloadState.DOWNLOADING) {
                                    CircularProgressIndicator(
                                        progress = { progress.coerceIn(0f, 1f) },
                                        modifier = Modifier.size(44.dp),
                                        trackColor = Color.White.copy(alpha = 0.2f),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                                IconButton(onClick = onTogglePause) {
                                    Icon(
                                        when (entity.state) {
                                            DownloadState.PAUSED -> Icons.Default.PlayArrow
                                            DownloadState.FAILED -> Icons.Default.Refresh
                                            else -> Icons.Default.Pause
                                        },
                                        when (entity.state) {
                                            DownloadState.PAUSED -> stringResource(R.string.resume_download)
                                            DownloadState.FAILED -> stringResource(R.string.retry_download)
                                            else -> stringResource(R.string.pause_download)
                                        },
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = onCancel, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Delete, stringResource(R.string.delete_download_2), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                    }
                }
            }

            if (!isFinished) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

private fun DownloadEntity.toPlaybackSong() = SongModel(
    id = songId,
    title = title,
    artist = artist,
    thumbnail = artworkUrl,
    duration = "0",
    provider = Provider.LOCAL.name,
    url = savedPath,
    album = album,
    year = year,
    extensionName = extensionName,
    composer = composer,
    genre = genre
)
