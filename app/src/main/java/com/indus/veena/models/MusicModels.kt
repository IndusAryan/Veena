package com.indus.veena.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class Provider { Saavn, YTMusic, NEWPIPE, SoundCloud, Tidal, LOCAL }

@Parcelize
data class SongModel(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String,
    val duration: String,
    val streamableUrls: Map<String, String> = emptyMap(),
    val provider: String,
    val url: String = "",
    val album: String = "",
    val albumArtist: String? = null,
    val composer: String,
    val genre: String,
    val lyricist: String? = null,
    val year: String,
) : Parcelable

data class AlbumModel(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String
)

data class ArtistModel(
    val id: String,
    val name: String,
    val imageUrl: String
)
