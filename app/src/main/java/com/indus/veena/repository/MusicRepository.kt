package com.indus.veena.repository

import android.R.attr.country
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.indus.veena.contract.*
import com.indus.veena.database.DataStoreKeys.SUGGESTION_PROVIDER_KEY
import com.indus.veena.di.ExtensionModule.json
import com.indus.veena.extension.ExtensionManager
import com.indus.veena.models.ItunesSearchResponse
import com.indus.veena.models.SongModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val extensionManager: ExtensionManager,
    private val dataStore: DataStore<Preferences>,
    private val okHttpClient: OkHttpClient,
) {
    data class CatalogItem(val id: String, val title: String, val subtitle: String,
        val thumbnail: String, val provider: String, val kind: String)
    data class DiscoverySection(val id: String, val title: String, val subtitle: String,
        val songs: List<SongModel>, val collections: List<CatalogItem> = emptyList())
    data class CollectionPage(val title: String, val subtitle: String, val thumbnail: String,
        val description: String, val songs: List<SongModel>, val hasMore: Boolean = false)
    data class ArtistSongPage(val songs: List<SongModel>, val hasMore: Boolean)
    val discoveryProviders = extensionManager.extensions.map { map ->
        map.values.filter { it.manifest.supports("discovery") && it.addon is DiscoveryAddon }
            .map { ProviderItem(it.manifest.id, it.manifest.name) }
    }
    private fun catalogItems(provider: String, albums: List<ExtAlbum>, artists: List<ExtArtist>) =
        albums.filter { it.id.isNotBlank() }.distinctBy { it.id }.map {
            CatalogItem(it.id, it.title, it.artist, it.thumbnail, provider, "album")
        } + artists.filter { it.id.isNotBlank() }.distinctBy { it.id }.map {
            CatalogItem(it.id, it.name, it.bio, it.thumbnail, provider, "artist")
        }
    suspend fun discover(provider: String): List<DiscoverySection> = withContext(Dispatchers.IO) {
        val extension = extensionManager.getById(provider) ?: error(context.getString(com.indus.veena.R.string.addon_removed))
        val addon = extension.addon as? DiscoveryAddon
            ?: error(context.getString(com.indus.veena.R.string.update_discovery_addon))
        if (addon is CatalogAddon) addon.discoverCatalog().distinctBy { it.id }.map { section ->
            DiscoverySection(section.id, section.title, section.subtitle,
                section.songs.filter { it.id.isNotBlank() }.distinctBy { it.id }
                    .map { it.toSongModel(provider, extension.manifest.name) },
                catalogItems(provider, section.albums, section.artists))
        }.filter { it.songs.isNotEmpty() || it.collections.isNotEmpty() }
        else addon.discover().distinctBy { it.id }.map { section ->
            DiscoverySection(section.id, section.title, section.subtitle,
                section.songs.filter { it.id.isNotBlank() }.distinctBy { it.id }
                    .map { it.toSongModel(provider, extension.manifest.name) })
        }.filter { it.songs.isNotEmpty() }
    }
    suspend fun searchDiscovery(provider: String, query: String, page: Int = 1): List<DiscoverySection> = withContext(Dispatchers.IO) {
        val extension = extensionManager.getById(provider) ?: error(context.getString(com.indus.veena.R.string.addon_removed))
        val addon = extension.addon
        val results = if (addon is CatalogAddon) addon.searchCatalog(query, page) else addon.search(query, page)
        buildList {
            val collections = if (addon is CatalogAddon) catalogItems(provider, results.albums, results.artists) else emptyList()
            val artists = collections.filter { it.kind == "artist" }
            val albums = collections.filter { it.kind == "album" }
            if (artists.isNotEmpty()) add(DiscoverySection("search:artists", context.getString(com.indus.veena.R.string.artists), "", emptyList(), artists))
            if (albums.isNotEmpty()) add(DiscoverySection("search:albums", context.getString(com.indus.veena.R.string.albums), "", emptyList(), albums))
            val songs = results.songs.filter { it.id.isNotBlank() }.distinctBy { it.id }
                .map { it.toSongModel(provider, extension.manifest.name) }
            if (songs.isNotEmpty()) add(DiscoverySection("search:songs", context.getString(com.indus.veena.R.string.songs), "", songs))
        }
    }
    suspend fun collection(provider: String, id: String, kind: String): CollectionPage = withContext(Dispatchers.IO) {
        val extension = extensionManager.getById(provider) ?: error(context.getString(com.indus.veena.R.string.addon_removed))
        val addon = extension.addon as? CatalogAddon ?: error(context.getString(com.indus.veena.R.string.collection_unsupported))
        val page = when (kind) { "album" -> addon.getAlbum(id); "artist" -> addon.getArtist(id)
            else -> error(context.getString(com.indus.veena.R.string.collection_unsupported)) }
        CollectionPage(page.title, page.subtitle, page.thumbnail, page.description,
            page.songs.filter { it.id.isNotBlank() }.distinctBy { it.id }.map { it.toSongModel(provider, extension.manifest.name) })
    }

    suspend fun artistSongs(provider: String, id: String, page: Int): ArtistSongPage? = withContext(Dispatchers.IO) {
        val extension = extensionManager.getById(provider) ?: error(context.getString(com.indus.veena.R.string.addon_removed))
        val addon = extension.addon as? com.indus.veena.contract.PagedArtistAddon ?: return@withContext null
        val result = addon.getArtistSongs(id, page)
        ArtistSongPage(result.songs.filter { it.id.isNotBlank() }.distinctBy { it.id }
            .map { it.toSongModel(provider, extension.manifest.name) }, result.hasMore)
    }

    private val detailsMutex = kotlinx.coroutines.sync.Mutex()
    private data class CachedDetails(val addon: com.indus.veena.contract.MusicAddon, val song: SongModel, val at: Long)
    private val detailsCache = linkedMapOf<String, CachedDetails>()

    private val TAG = "VEENA_DEX"
    data class ProviderItem(val id: String, val name: String)
    val availableProviders: Flow<List<ProviderItem>> = extensionManager.extensions.map { map ->
        map.values.map { ProviderItem(id = it.manifest.id, name = it.manifest.name) }
    }
    private val extensionsReady = kotlinx.coroutines.CompletableDeferred<Unit>()
    suspend fun awaitExtensionsLoaded() = extensionsReady.await()

    private val _noAddonsAvailable = MutableStateFlow(false)
    val noAddonsAvailable: StateFlow<Boolean> = _noAddonsAvailable
    init {
        Log.d(TAG, "MusicRepository init. Scanning for plugins...")
        CoroutineScope(Dispatchers.IO).launch {
            try { syncAndLoadExtensions() } finally { extensionsReady.complete(Unit) }
            extensionManager.extensions.collect { extensions ->
                _noAddonsAvailable.value = extensions.isEmpty()
            }
        }
    }

    val availableSuggestionProviders: Flow<List<ProviderItem>> = extensionManager.extensions.map { map ->
        val items = map.values
            .filter { it.manifest.supports("suggestions") }
            .map { ProviderItem(id = it.manifest.id, name = it.manifest.name) }
            .toMutableList()
        items.add(0, ProviderItem(id = "itunes", name = "iTunes"))
        items
    }

    val currentSuggestionProviderId: Flow<String> = dataStore.data.map { prefs ->
        prefs[SUGGESTION_PROVIDER_KEY] ?: "itunes"
    }

    private suspend fun syncAndLoadExtensions() {
        val internalDir = File(context.filesDir, "extensions").apply { mkdirs() }
        internalDir.listFiles()
            ?.filter { it.extension.lowercase() in listOf("js", "veena") }
            ?.forEach { file -> runCatching { extensionManager.loadFile(file) }
                .onFailure { Log.e(TAG, "Cannot load ${file.name}", it) } }
    }

    suspend fun searchSongs(query: String, providerName: String): List<SongModel> = withContext(Dispatchers.IO) {
        val extension = extensionManager.getById(providerName) ?: return@withContext emptyList()
        return@withContext try {
            extension.addon.searchSongs(query).map { it.toSongModel(providerName, extension.manifest.name) }
        } catch (t: Throwable) { // Catches LinkageError, NoSuchMethodError, and Exceptions
            Log.e(TAG, "Search failed", t)
            emptyList()
        }
    }

    suspend fun getSongDetails(id: String, providerName: String, forceRefresh: Boolean = false): SongModel? = withContext(Dispatchers.IO) {
        detailsMutex.withLock {
            val extension = extensionManager.getById(providerName) ?: return@withLock null
            val key = "$providerName:$id"
            if (forceRefresh) detailsCache.remove(key)
            val now = android.os.SystemClock.elapsedRealtime()
            detailsCache[key]?.let { cached ->
                if (!forceRefresh && cached.addon === extension.addon && now - cached.at < 60_000L) {
                    return@withLock cached.song
                }
            }
            try {
                val policy = extension.addon as? com.indus.veena.contract.MediaRequestAddon
                val song = extension.addon.getSongDetails(id).toSongModel(providerName, extension.manifest.name).copy(
                    // A resolver may canonicalize a URL; UI, database, and work identity must
                    // remain the ID that was selected, including when downloading during playback.
                    id = id,
                    playbackHeaders = policy?.getPlaybackHeaders(id).orEmpty(),
                    downloadHeaders = policy?.getDownloadHeaders(id).orEmpty())
                if (song.streamableUrls.isNotEmpty()) {
                    detailsCache[key] = CachedDetails(extension.addon, song, now)
                    if (detailsCache.size > 40) detailsCache.remove(detailsCache.keys.first())
                }
                song
            } catch (e: CancellationException) { throw e }
            catch (t: Throwable) {
                Log.e(TAG, "Details failed for $providerName", t)
                throw Exception("${extension.manifest.name}: ${t.message ?: context.getString(com.indus.veena.R.string.could_not_resolve_stream)}", t)
            }
        }
    }

    suspend fun getSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        val prefs = dataStore.data.first()
        val providerId = prefs[SUGGESTION_PROVIDER_KEY] ?: "itunes"

        if (providerId == "itunes") {
            return@withContext fetchItunesSuggestions(query, countryCode = Locale.getDefault().country)
        }

        val extension = extensionManager.getById(providerId) ?: return@withContext emptyList()
        if (!extension.manifest.supports("suggestions")) {
            return@withContext emptyList()
        }

        return@withContext try {
            extension.addon.getSuggestions(query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun fetchItunesSuggestions(query: String, countryCode: String = "IN"): List<String> {
        if (query.isBlank()) return emptyList()
        val url = "https://itunes.apple.com/search?term=$query&media=music&limit=25&country=$countryCode"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "iTunes/12.9.5 (Windows; Microsoft Windows 10.0 x64; Edition Professional) AppleWebKit/7606.1002.2005.1 (KHTML, like Gecko)")
            .header("Accept", "application/json")
            .build()

        return try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body.string()
                val result = json.decodeFromString<ItunesSearchResponse>(body)
                result.results.mapNotNull { it.trackName }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "iTunes suggestions failed", t)
            emptyList()
        }
    }

    private fun ExtSong.toSongModel(provider: String, extensionName: String): SongModel {
        return SongModel(
            id = this.id,
            title = this.title,
            artist = this.artist,
            thumbnail = this.thumbnail,
            duration = this.duration,
            streamableUrls = this.streamableUrls ?: emptyMap(),
            provider = provider,
            extensionName = extensionName,
            url = this.url,
            album = this.album,
            albumArtist = this.albumArtist,
            composer = this.composer,
            genre = this.genre,
            lyricist = this.lyricist,
            year = this.year
        )
    }

   // private fun ExtAlbum.toDomain(): AlbumModel = AlbumModel(id, title, artist, coverUrl)
    //private fun ExtArtist.toDomain(): ArtistModel = ArtistModel(id, name, imageUrl)
}