package com.indus.veena.ui.screens.player

import com.indus.veena.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indus.veena.ui.components.SongArtwork
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.models.SongModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongDetailsSheet(
    song: SongModel,
    duration: Long,
    downloadEntity: DownloadEntity?,
    accentColor: Color,
    onDismiss: () -> Unit
) {
    val credits = listOf(
            stringResource(R.string.album) to song.album,
            stringResource(R.string.album_artist) to song.albumArtist,
            stringResource(R.string.year) to song.year,
            stringResource(R.string.genre) to song.genre,
            stringResource(R.string.composer) to song.composer,
            stringResource(R.string.lyrics_by) to song.lyricist
        ).filter { (_, value) -> !value.isNullOrBlank() && !value.equals("unknown", true) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF17191F),
        contentColor = Color.White,
        dragHandle = {
            Surface(
                color = Color.White.copy(alpha = 0.25f), shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(32.dp, 4.dp)
            ) {}
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                stringResource(R.string.about_this_song), color = accentColor,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SongArtwork(
                    song.thumbnail, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        song.artist,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val durationText = if (duration > 1L) formatTime(duration) else song.duration
                    if (durationText.isNotBlank()) {
                        Text(
                            durationText,
                            color = accentColor,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            if (credits.isNotEmpty()) {
                DetailGroup(stringResource(R.string.record_credits), accentColor) {
                    credits.forEach { (label, value) -> DetailValue(label, value.orEmpty()) }
                }
            }
            DetailGroup(stringResource(R.string.listening_details), accentColor) {
                DetailValue(stringResource(R.string.addon), if (song.provider == "LOCAL") stringResource(R.string.on_this_device)
                    else song.extensionName.ifBlank { song.provider })
                if (song.streamableUrls.isNotEmpty()) {
                    DetailValue(
                        stringResource(R.string.available_audio_qualities),
                        song.streamableUrls.keys.joinToString(" · ")
                    )
                }
                DetailValue(
                    stringResource(R.string.download), when (downloadEntity?.state) {
                        DownloadState.COMPLETED -> stringResource(R.string.saved_for_offline_listening)
                        DownloadState.FAILED -> stringResource(R.string.download_failed)
                        null -> if (song.provider == "LOCAL") stringResource(R.string.local_audio_file) else stringResource(R.string.not_downloaded)
                        else -> stringResource(R.string.downloading_with_progress, downloadEntity.progress)
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailGroup(title: String, accentColor: Color, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = accentColor)
        Surface(
            color = Color.White.copy(alpha = 0.04f),
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DetailValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
