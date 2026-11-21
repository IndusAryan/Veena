package com.indus.veena.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.indus.veena.R

/** Missing artwork is an ordinary provider result, not a network request for an empty URL. */
@Composable
fun SongArtwork(model: String, contentDescription: String?, modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop) {
    val placeholder = painterResource(R.drawable.ic_music_library)
    if (model.isBlank()) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(placeholder, contentDescription, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        AsyncImage(model = model, contentDescription = contentDescription, modifier = modifier,
            contentScale = contentScale, placeholder = placeholder, error = placeholder)
    }
}
