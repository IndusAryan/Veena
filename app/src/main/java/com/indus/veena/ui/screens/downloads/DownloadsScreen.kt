package com.indus.veena.ui.screens.downloads

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.indus.veena.models.SongModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onSongClick: (SongModel) -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            if (downloads.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Download, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text("No active downloads", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(downloads, key = { it.songId }) { entity ->
                DownloadItemCard(
                    entity = entity,
                    onCardClick = {
                        if (entity.state == DownloadState.COMPLETED) {
                            val localSong = SongModel(
                                id = entity.songId,
                                title = entity.title,
                                artist = entity.artist,
                                thumbnail = entity.artworkUrl,
                                duration = "0", // Player will resolve actual duration from file
                                provider = Provider.LOCAL.name, // Marks it as local
                                url = entity.savedPath,    // The local file path
                                album = entity.album,
                                year = entity.year,
                                composer = entity.composer,
                                genre = entity.genre
                            )
                            onSongClick(localSong)
                        }
                    },
                    onTogglePause = { viewModel.togglePause(entity.songId, entity.state == DownloadState.DOWNLOADING) },
                    onCancel = { viewModel.removeDownload(entity.songId) }
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
        modifier = Modifier.fillMaxWidth().height(110.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = { if (entity.state == DownloadState.COMPLETED) onCardClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Beautiful Blurred Background
            AsyncImage(
                model = entity.artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(30.dp, BlurredEdgeTreatment.Unbounded)
            )
            // Overlay to darken the blur so text is readable
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)))
            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = entity.artworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(86.dp).clip(RoundedCornerShape(12.dp))
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
                            text = entity.comment.replace("Veena: ", ""),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (!isFinished) {
                        Text(
                            text = "${entity.state.name} · $mbDownloaded / $mbTotal MB",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isFinished) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = containerColor, modifier = Modifier.size(32.dp))
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            CircularWavyProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(42.dp),
                                trackColor = Color.White.copy(alpha = 0.2f),
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = onTogglePause, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    if (entity.state == DownloadState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    null, tint = Color.White, modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    IconButton(onClick = onCancel, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, "Cancel", tint = Color.Red, modifier = Modifier.size(18.dp))
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