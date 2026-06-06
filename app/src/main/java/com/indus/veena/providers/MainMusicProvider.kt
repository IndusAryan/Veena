package com.indus.veena.providers

import com.indus.veena.models.AlbumModel
import com.indus.veena.models.ArtistModel
import com.indus.veena.models.Provider
import com.indus.veena.models.SongModel

abstract class MainMusicProvider {
    abstract val providerName: Provider
    abstract val baseUrl: String

    // Standardized search methods
    abstract suspend fun searchSongs(query: String, page: Int = 1): List<SongModel>
    abstract suspend fun searchAlbums(query: String, page: Int = 1): List<AlbumModel>
    abstract suspend fun searchArtists(query: String, page: Int = 1): List<ArtistModel>

    // Details
    abstract suspend fun getSongDetails(songId: String): SongModel
    abstract suspend fun getAlbumDetails(albumId: String): AlbumModel?
}