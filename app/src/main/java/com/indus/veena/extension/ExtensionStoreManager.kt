package com.indus.veena.extension

import android.content.Context
import android.util.Log
import com.indus.veena.di.ExtensionModule.json
import com.indus.veena.models.GithubRelease
import com.indus.veena.models.UpdateInfo
import com.indus.veena.util.formatSize
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CatalogExtensionItem(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val downloadUrl: String,
    val iconUrl: String,
    val description: String,
    val author: String,
    val isOfficial: Boolean,
    val size: String,
    val capabilities: List<String>
)

@Singleton
class ExtensionStoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val extensionManager: ExtensionManager
) {
    private val catalogUrl = "https://raw.githubusercontent.com/IndusAryan/Veena/refs/heads/main/app/src/main/assets/catalog.json"
    private val TAG = "ExtensionStoreManager"

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress = _downloadProgress.asStateFlow()

    suspend fun fetchCatalog(): List<CatalogExtensionItem> = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(catalogUrl).build()
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                json.decodeFromString<List<CatalogExtensionItem>>(response.body.string())
            }
        } catch (e: Exception) {
            context.assets.open("catalog.json").use { input ->
                val localJson = input.bufferedReader().readText()
                json.decodeFromString<List<CatalogExtensionItem>>(localJson)
            }
        }
    }

    private val manifestCache = java.util.concurrent.ConcurrentHashMap<String, com.indus.veena.contract.ExtensionManifest>()
    private val installMutex = kotlinx.coroutines.sync.Mutex()
    private val _updateEvents = kotlinx.coroutines.channels.Channel<String>(kotlinx.coroutines.channels.Channel.UNLIMITED)
    val updateEvents = _updateEvents.receiveAsFlow()

    private fun readManifest(file: File): com.indus.veena.contract.ExtensionManifest =
        java.util.zip.ZipFile(file).use { zip ->
            val entry = requireNotNull(zip.getEntry("assets/manifest.json")) { context.getString(com.indus.veena.R.string.addon_manifest_missing) }
            json.decodeFromString(zip.getInputStream(entry).bufferedReader().readText())
        }

    private fun validate(manifest: com.indus.veena.contract.ExtensionManifest, id: String) {
        manifest.requireCompatible(id)
    }

    private fun downloadPackage(url: String, target: File, progress: (Float) -> Unit = {}) {
        okHttpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
            check(response.isSuccessful) { "HTTP ${response.code}" }
            val length = response.body.contentLength()
            response.body.byteStream().use { input -> target.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var copied = 0L
                var lastPercent = -1
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                    copied += read
                    if (length > 0) {
                        val percent = ((copied * 100) / length).toInt()
                        if (percent != lastPercent) {
                            lastPercent = percent
                            progress((copied.toFloat() / length).coerceIn(0f, 1f))
                        }
                    }
                }
            } }

        }
    }

    suspend fun installExtension(item: CatalogExtensionItem, onlyIfNewer: Boolean = false): Boolean =
        withContext(Dispatchers.IO) {
            installMutex.withLock {
                require(item.id.matches(Regex("[a-z0-9_]+"))) { context.getString(com.indus.veena.R.string.addon_invalid_id) }
                _downloadProgress.update { it + (item.id to 0f) }
                val targetDir = File(context.filesDir, "extensions").apply { mkdirs() }
                val tempFile = File.createTempFile("addon-", ".tmp", targetDir)
                val finalFile = File(targetDir, "${item.id}.veena")
                val backup = File(targetDir, "${item.id}.backup")
                try {
                    downloadPackage(item.downloadUrl, tempFile) { progress ->
                        _downloadProgress.update { it + (item.id to progress) }
                    }
                    currentCoroutineContext().ensureActive()
                    val manifest = readManifest(tempFile)
                    validate(manifest, item.id)
                    val previous = extensionManager.getById(item.id)?.manifest
                    if (onlyIfNewer && (previous == null || manifest.versionCode <= previous.versionCode)) {
                        return@withLock false
                    }
                    if (finalFile.exists()) check(finalFile.renameTo(backup)) { context.getString(com.indus.veena.R.string.addon_backup_failed) }
                    check(tempFile.renameTo(finalFile)) { context.getString(com.indus.veena.R.string.addon_save_failed) }
                    try {
                        extensionManager.loadFile(finalFile)
                    } catch (t: Throwable) {
                        finalFile.delete()
                        if (backup.exists()) check(backup.renameTo(finalFile)) { context.getString(com.indus.veena.R.string.addon_restore_failed) }
                        throw t
                    }
                    backup.delete()
                    val updated = previous != null && manifest.versionCode > previous.versionCode
                    if (updated) _updateEvents.trySend(context.getString(com.indus.veena.R.string.addon_updated, manifest.name, manifest.version))
                    updated
                } finally {
                    tempFile.delete()
                    // Also restore a backup when saving the replacement failed.
                    if (!finalFile.exists() && backup.exists()) backup.renameTo(finalFile)
                    _downloadProgress.update { it - item.id }
                }
            }
        }

    suspend fun updateInstalledAddons() {
        val catalog = fetchCatalog()
        extensionManager.extensions.value.values.toList().forEach { installed ->
            val manifest = installed.manifest
            val item = catalog.find { it.id == manifest.id }
            val source = manifest.updateUrl.ifBlank { item?.downloadUrl.orEmpty() }
            try {
                val info = fetchGithubUpdateInfo(source, manifest.id) ?: return@forEach
                if (info.versionCode > manifest.versionCode) {
                    installExtension(CatalogExtensionItem(manifest.id, manifest.name, info.version,
                        info.versionCode, info.downloadUrl, manifest.iconUrl, manifest.description,
                        manifest.author, false, info.size, manifest.capabilities), onlyIfNewer = true)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Auto-update failed for ${manifest.id}", e)
            }
        }
    }

    suspend fun fetchGithubUpdateInfo(downloadOrReleaseUrl: String, addonId: String): UpdateInfo? = withContext(Dispatchers.IO) {
        if (!downloadOrReleaseUrl.contains("github.com")) return@withContext null

        // Correctly matches owner and repo only (e.g., owner = Dp3seoQne, repo = VeenaCommunityAddons)
        val match = Regex("""github\.com/([^/]+)/([^/]+)""").find(downloadOrReleaseUrl) ?: return@withContext null
        val owner = match.groupValues[1]
        val repo = match.groupValues[2]

        // Clean target API endpoint
        val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"

        val request = Request.Builder()
            .url(apiUrl)
            .header("Accept", "application/vnd.github.v3+json")
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val body = response.body.string()
                val release = json.decodeFromString<GithubRelease>(body)

                // Matches the asset file for this specific addon (e.g. np.veena, sc.veena, etc.)
                val matchingAsset = release.assets.find {
                    it.name.equals("$addonId.veena", ignoreCase = true)
                } ?: return@withContext null
                val candidate = File.createTempFile("update-", ".veena", context.cacheDir)
                try {
                    val cacheKey = "${matchingAsset.browser_download_url}:${matchingAsset.updated_at}:${matchingAsset.id}"
                    val manifest = manifestCache[cacheKey] ?: run {
                        downloadPackage(matchingAsset.browser_download_url, candidate)
                        readManifest(candidate).also { parsed ->
                            validate(parsed, addonId)
                            manifestCache[cacheKey] = parsed
                        }
                    }
                    validate(manifest, addonId)
                    UpdateInfo(manifest.version, matchingAsset.browser_download_url,
                        formatSize(matchingAsset.size), manifest.versionCode)
                } finally {
                    candidate.delete()
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check update metadata for $addonId", e)
            null
        }
    }

    suspend fun deleteExtension(id: String) = withContext(Dispatchers.IO) {
        installMutex.withLock {
        try {
            extensionManager.unloadExtension(id)
            val targetDir = File(context.filesDir, "extensions")
            val file = File(targetDir, "$id.veena")
            if (file.exists()) {
                if (!file.delete()) throw Exception(context.getString(com.indus.veena.R.string.file_delete_failed))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete physical file for extension: $id", e)
            throw e
        }
        }
    }
}