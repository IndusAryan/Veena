package com.indus.veena.contract

import kotlinx.serialization.Serializable

/** Optional richer discovery. Existing DiscoveryAddon constructors and DEX ABI stay unchanged.
 * Only return albums/artists for which getAlbum/getArtist can load tracks. Calls run on host IO.
 * IDs are opaque provider IDs; never substitute a title or a search for collection membership.
 */
interface CatalogAddon : DiscoveryAddon {
    fun discoverCatalog(): List<ExtCatalogSection>
    fun searchCatalog(query: String, page: Int = 1): ExtSearchResults
    fun getAlbum(id: String): ExtCollection
    fun getArtist(id: String): ExtCollection
}

@Serializable
data class ExtCatalogSection(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val songs: List<ExtSong> = emptyList(),
    val albums: List<ExtAlbum> = emptyList(),
    val artists: List<ExtArtist> = emptyList()
)

/** A provider collection with its available tracks. */
@Serializable
data class ExtCollection(
    val title: String,
    val subtitle: String = "",
    val thumbnail: String = "",
    val description: String = "",
    val songs: List<ExtSong> = emptyList()
)

/** Optional paged artist songs; page numbers start at zero. */
interface PagedArtistAddon {
    fun getArtistSongs(id: String, page: Int): ExtArtistSongPage
}

@Serializable
data class ExtArtistSongPage(val songs: List<ExtSong> = emptyList(), val hasMore: Boolean = false)
