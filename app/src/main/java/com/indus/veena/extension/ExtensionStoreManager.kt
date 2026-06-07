package com.indus.veena.extension

import android.content.Context
import android.util.Log
import com.indus.veena.di.ExtensionModule.json
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
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
    private val catalogUrl = "https://raw.githubusercontent.com/IndusVeena/VeenaCommunityAddons/master/catalog.json"
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

    suspend fun installExtension(item: CatalogExtensionItem) = withContext(Dispatchers.IO) {
        val request = Request.Builder().url(item.downloadUrl).build()
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

                val targetDir = File(context.filesDir, "extensions").apply { mkdirs() }
                val tempFile = File(targetDir, "${item.id}.tmp")

                val totalBytes = response.body.contentLength()
                var bytesCopied = 0L

                response.body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytes = input.read(buffer)
                        while (bytes >= 0) {
                            output.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            if (totalBytes > 0) {
                                val progress = bytesCopied.toFloat() / totalBytes
                                _downloadProgress.update { it + (item.id to progress) }
                            }
                            bytes = input.read(buffer)
                        }
                    }
                }

                val finalFile = File(targetDir, "${item.id}.veena")
                if (finalFile.exists()) finalFile.delete()
                tempFile.renameTo(finalFile)

                _downloadProgress.update { it - item.id }
                extensionManager.loadFile(finalFile)
            }
        } catch (e: Exception) {
            _downloadProgress.update { it - item.id }
            Log.e(TAG, "Failed to download/install extension: ${item.id}", e)
            throw e
        }
    }

    suspend fun deleteExtension(id: String) = withContext(Dispatchers.IO) {
        try {
            extensionManager.unloadExtension(id)
            val targetDir = File(context.filesDir, "extensions")
            val file = File(targetDir, "$id.veena")
            if (file.exists()) {
                if (!file.delete()) throw Exception("File deletion failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete physical file for extension: $id", e)
            throw e
        }
    }
}