package com.indus.veena.extension

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.asyncFunction
import com.dokar.quickjs.binding.function
import com.indus.veena.contract.ExtSong
import com.indus.veena.contract.ExtensionHost
import com.indus.veena.contract.ExtensionManifest
import com.indus.veena.contract.MusicAddon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface JsExtensionFactory {
    suspend fun create(script: String, manifest: ExtensionManifest): MusicAddon
}

class JsMusicExtension(
    private val script: String,
    val manifest: ExtensionManifest,
    private val host: ExtensionHost
) : MusicAddon {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var quickJs: QuickJs? = null
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun onLoad(host: ExtensionHost) = runBlocking {
        quickJs = QuickJs.create(Dispatchers.IO).apply {
            asyncFunction("__httpGet") { args ->
                val url = args[0] as String
                host.httpGet(url)
            }
            asyncFunction("__httpPost") { args ->
                val url = args[0] as String
                val body = args[1] as String
                host.httpPost(url = url, body = body)
            }
            function("__decrypt") { args ->
                val payload = args[0] as String
                val extractor = args[1] as String
                host.decrypt(payload, extractor)
            }

            evaluate<Any>("""
                const host = {
                    httpGet: async (url) => await __httpGet(url),
                    httpPost: async (url, body) => await __httpPost(url, body),
                    decrypt: (payload) => __decrypt(payload)
                };
            """.trimIndent())

            evaluate<Any>(script)
        }
    }

    override fun onUnload() {
        scope.cancel()
        quickJs?.close()
        quickJs = null
    }

    private inline fun <reified T> executeJs(expression: String): T = runBlocking(scope.coroutineContext) {
        val engine = quickJs ?: throw Exception("JS Context not loaded")
        val jsonResult = engine.evaluate<String>("JSON.stringify(await $expression)")
        json.decodeFromString<T>(jsonResult)
    }

    override fun searchSongs(query: String, page: Int): List<ExtSong> {
        val encodedQuery = json.encodeToString(query)
        return executeJs("searchSongs($encodedQuery, $page)")
    }

    override fun getSongDetails(songId: String): ExtSong {
        return executeJs("getSongDetails('$songId')")
    }

    override fun getStreamUrl(songId: String, quality: String): String {
        return executeJs("getStreamUrl('$songId', '$quality')")
    }
}