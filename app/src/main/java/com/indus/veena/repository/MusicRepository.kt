package com.indus.veena.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.indus.veena.contract.ExtSong
import com.indus.veena.database.DataStoreKeys.SUGGESTION_PROVIDER_KEY
import com.indus.veena.extension.ExtensionManager
import com.indus.veena.models.SongModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val extensionManager: ExtensionManager,
    private val dataStore: DataStore<Preferences>
) {
    private val TAG = "VEENA_DEX"
    data class ProviderItem(val id: String, val name: String)
    val availableProviders: Flow<List<ProviderItem>> = extensionManager.extensions.map { map ->
        map.values.map { ProviderItem(id = it.manifest.id, name = it.manifest.name) }
    }

    init {
        Log.d(TAG, "MusicRepository init. Scanning for plugins...")
        CoroutineScope(Dispatchers.IO).launch {
            syncAndLoadExtensions()
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
        //val externalDir = context.getExternalFilesDir("extensions")

        /*externalDir?.listFiles()?.forEach { externalFile ->
            if (externalFile.extension.lowercase() in listOf("js", "veena")) {
                val target = File(internalDir, externalFile.name)
                if (!target.exists() || externalFile.lastModified() > target.lastModified()) {
                    target.setWritable(true) // reset in case it was previously locked
                    externalFile.copyTo(target, overwrite = true)
                    target.setReadOnly() // lock before loader touches it
                    Log.d(TAG, "Synced: ${externalFile.name}")
                }
            }
        }*/
        internalDir.listFiles()
            ?.filter { it.extension.lowercase() in listOf("js", "veena") }
            ?.forEach { extensionManager.loadFile(it) }
    }

    suspend fun searchSongs(query: String, providerName: String): List<SongModel> = withContext(Dispatchers.IO) {
        val extension = extensionManager.getById(providerName) ?: return@withContext emptyList()
        return@withContext try {
            extension.addon.searchSongs(query).map { it.toSongModel(providerName) }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed", e)
            emptyList()
        }
    }

    suspend fun getSongDetails(id: String, providerName: String): SongModel? = withContext(Dispatchers.IO) {
        val extension = extensionManager.getById(providerName) ?: return@withContext null
        return@withContext try {
            extension.addon.getSongDetails(id).toSongModel(providerName)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getSuggestions(query: String): List<String> {
        val prefs = dataStore.data.first()
        val providerId = prefs[SUGGESTION_PROVIDER_KEY] ?: "itunes"

        if (providerId == "itunes") {
            return emptyList()
        }

        val extension = extensionManager.getById(providerId) ?: return emptyList()
        if (!extension.manifest.supports("suggestions")) {
            return emptyList()
        }

        return try {
            extension.addon.getSuggestions(query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun ExtSong.toSongModel(provider: String): SongModel {
        return SongModel(
            id = this.id,
            title = this.title,
            artist = this.artist,
            thumbnail = this.thumbnail,
            duration = this.duration,
            streamableUrls = this.streamableUrls ?: emptyMap(),
            provider = provider,
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