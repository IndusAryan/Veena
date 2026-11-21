package com.indus.veena.ui.screens.favourites

import com.indus.veena.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indus.veena.database.sqlite.daos.FavouriteDao
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.lifecycle.ioScope
import com.indus.veena.database.sqlite.entities.FavouriteEntity
import com.indus.veena.models.PlaybackQueue
import com.indus.veena.models.SongModel
import com.indus.veena.ui.screens.downloads.DownloadItemCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val favouriteDao: FavouriteDao
) : ViewModel() {
    val favourites = favouriteDao.getAllFavourites()

    fun removeFavourite(songId: String) {
        ioScope {
            favouriteDao.deleteBySongId(songId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onSongClick: (PlaybackQueue) -> Unit,
    viewModel: FavouritesViewModel = hiltViewModel()
) {
    val favourites by viewModel.favourites.collectAsState(initial = emptyList())

    val favouritesSource = stringResource(R.string.favourites)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favourites), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (favourites.isEmpty()) {
            // Centre independently of the outgoing navigation bar's animated padding.
            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Favorite, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.no_favourites_yet), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(favourites, key = { it.songId }) { fav ->
                DownloadItemCard(
                    entity = DownloadEntity(
                        songId = fav.songId,
                        title = fav.title,
                        artist = fav.artist,
                        artworkUrl = fav.thumbnail,
                        url = fav.url,
                        customHeaders = "",
                        album = fav.album,
                        year = fav.year,
                        composer = fav.composer,
                        extensionName = fav.extensionName,
                        genre = fav.genre,
                        state = DownloadState.COMPLETED
                    ),
                    onCardClick = {
                        onSongClick(PlaybackQueue.from(favouritesSource, favourites.map { it.toPlaybackSong() }, fav.toPlaybackSong()))
                    },
                    onTogglePause = {},
                    onCancel = { viewModel.removeFavourite(fav.songId) }
                )
            }
        }
    }
}

internal fun FavouriteEntity.toPlaybackSong() = SongModel(
    id = songId,
    title = title,
    artist = artist,
    thumbnail = thumbnail,
    duration = duration,
    url = url,
    extensionName = extensionName,
    provider = provider,
    album = album,
    year = year,
    composer = composer,
    genre = genre
)
