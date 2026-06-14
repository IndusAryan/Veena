package com.indus.veena.ui.screens.favourites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.indus.veena.database.sqlite.daos.FavouriteDao
import com.indus.veena.database.sqlite.entities.DownloadEntity
import com.indus.veena.database.sqlite.entities.DownloadState
import com.indus.veena.models.SongModel
import com.indus.veena.ui.screens.downloads.DownloadItemCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    favouriteDao: FavouriteDao
) : ViewModel() {
    val favourites = favouriteDao.getAllFavourites()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onSongClick: (SongModel) -> Unit,
    viewModel: FavouritesViewModel = hiltViewModel()
) {
    val favourites by viewModel.favourites.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favourites", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (favourites.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Favorite, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text("No favourites yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

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
                        onSongClick(SongModel(
                            id = fav.songId,
                            title = fav.title,
                            artist = fav.artist,
                            thumbnail = fav.thumbnail,
                            duration = fav.duration,
                            url = fav.url,
                            provider = fav.provider,
                            album = fav.album,
                            year = fav.year,
                            composer = fav.composer,
                            genre = fav.genre
                        ))
                    },
                    onTogglePause = {},
                    onCancel = {}
                )
            }
        }
    }
}