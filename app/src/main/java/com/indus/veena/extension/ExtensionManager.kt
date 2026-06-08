package com.indus.veena.extension

import android.content.Context
import android.util.Log
import com.indus.veena.contract.ExtensionError
import com.indus.veena.contract.ExtensionHost
import com.indus.veena.contract.ExtensionManifest
import com.indus.veena.contract.ExtensionType
import com.indus.veena.contract.LoadedExtension
import com.indus.veena.contract.MusicAddon
import com.indus.veena.di.ExtensionModule.json
import dagger.hilt.android.qualifiers.ApplicationContext
import dalvik.system.DexClassLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val host: ExtensionHost
) {
    private val _extensions = MutableStateFlow<Map<String, LoadedExtension>>(emptyMap())
    val extensions: StateFlow<Map<String, LoadedExtension>> = _extensions.asStateFlow()

    // test impl
    /*init {
        val localManifest = ExtensionManifest(
            id = "some_internal",
            name = "some_local",
            version = "1.0.0",
            versionCode = 1,
            apiVersion = 1,
            capabilities = listOf("search", "details", "stream", "suggestions"),
            description = "Local debug provider",
            author = "System"
        )
        val localAddon = NewPipeLocalPlugin().apply {
            onLoad(host)
        }
        _extensions.value = mapOf(
            "some_internal" to LoadedExtension(
                addon = localAddon,
                manifest = localManifest,
                type = ExtensionType.DEX
            )
        )
    }*/

    fun getById(id: String): LoadedExtension? = _extensions.value[id]

    suspend fun loadFile(file: File) = withContext(Dispatchers.IO) {
        try {
            val loaded = when (file.extension.lowercase()) {
                "js" -> loadJs(file)
                "veena" -> loadDex(file)
                else -> throw ExtensionError.ManifestError("Unknown extension type: ${file.extension}")
            }
            // Unload existing before replacing
            _extensions.value[loaded.manifest.id]?.addon?.onUnload()
            loaded.addon.onLoad(host)
            _extensions.update { it + (loaded.manifest.id to loaded) }
            Log.d(TAG, "Loaded extension: ${loaded.manifest.name} v${loaded.manifest.version}")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load extension: ${file.name}", t)
        }
    }

    private fun loadJs(file: File): LoadedExtension {
        val script = file.readText()
        val manifest = parseJsManifest(script)
        return LoadedExtension(
            addon = JsMusicExtension(script),
            manifest = manifest,
            type = ExtensionType.JS
        )
    }

    private fun loadDex(file: File): LoadedExtension {
        // Must be read-only before DexClassLoader opens it — Android 10+ requirement
        if (file.canWrite()) {
            file.setReadOnly()
        }

        val manifest = ZipFile(file).use { zip ->
            val entry = zip.getEntry("assets/manifest.json")
                ?: throw ExtensionError.ManifestError("Missing assets/manifest.json in ${file.name}")
            json.decodeFromString<ExtensionManifest>(
                zip.getInputStream(entry).bufferedReader().readText()
            )
        }

        val odexDir = File(context.codeCacheDir, "ext_${manifest.id}_${manifest.versionCode}_${file.lastModified()}")
            .also { it.mkdirs() }

        context.codeCacheDir.listFiles()
            ?.filter { it.isDirectory && it.name.startsWith("ext_${manifest.id}_") && it.name != odexDir.name }
            ?.forEach { it.deleteRecursively() }

        val classLoader = DexClassLoader(
            file.absolutePath,
            odexDir.absolutePath,
            null,
            context.classLoader
        )

        val addon = classLoader.loadClass(manifest.entryPoint)
            .getDeclaredConstructor()
            .newInstance() as? MusicAddon
            ?: throw ExtensionError.ManifestError("${manifest.entryPoint} does not implement MusicAddon")

        return LoadedExtension(addon = addon, manifest = manifest, type = ExtensionType.DEX)
    }

    private fun parseJsManifest(script: String): ExtensionManifest {
        val start = script.indexOf("{", script.indexOf("MANIFEST"))
        if (start == -1) throw ExtensionError.ManifestError("No MANIFEST block found in script.")

        var depth = 0
        var end = -1
        for (i in start until script.length) {
            when (script[i]) {
                '{' -> depth++
                '}' -> if (--depth == 0) { end = i; break }
            }
        }
        if (end == -1) throw ExtensionError.ManifestError("Mismatched braces in MANIFEST.")

        val raw = script.substring(start, end + 1)
            .replace(Regex("""([{,]\s*)([a-zA-Z_][a-zA-Z0-9_]*)\s*:""")) { "${it.groupValues[1]}\"${it.groupValues[2]}\":" }
            .replace(Regex("""'([^']*)'"""), "\"$1\"")

        return json.decodeFromString(raw)
    }

    suspend fun unloadAll() {
        _extensions.value.values.forEach { it.addon.onUnload() }
        _extensions.value = emptyMap()
    }

    suspend fun unloadExtension(id: String) = withContext(Dispatchers.Main) {
        val extension = _extensions.value[id]
        if (extension != null) {
            try {
                extension.addon.onUnload()
            } catch (e: Exception) {
                Log.e(TAG, "Error unloading addon: $id", e)
            }
            _extensions.update { it - id }
            Log.d(TAG, "Unloaded addon from memory: $id")
        }
    }

    companion object {
        private const val TAG = "ExtensionManager"
    }
}