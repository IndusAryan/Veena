package com.indus.veena.extension

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.asyncFunction
import com.dokar.quickjs.binding.function
import com.indus.veena.contract.*
import com.indus.veena.contract.ExtensionHost
import com.indus.veena.di.ExtensionModule.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking

class JsMusicExtension(
    private val script: String,
) : CatalogAddon {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var quickJs: QuickJs? = null

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
        val jsonResult = engine.evaluate<String>("JSON.stringify(await ($expression))")
        json.decodeFromString<T>(jsonResult)
    }

    override fun discover(): List<com.indus.veena.contract.ExtDiscoverySection> = executeJs("discover()")

    override fun discoverCatalog(): List<ExtCatalogSection> =
        if (executeJs<Boolean>("typeof discoverCatalog === 'function'")) executeJs("discoverCatalog()")
        else discover().map { ExtCatalogSection(it.id, it.title, it.subtitle, songs = it.songs) }

    override fun searchCatalog(query: String, page: Int): ExtSearchResults =
        if (executeJs<Boolean>("typeof searchCatalog === 'function'"))
            executeJs("searchCatalog(${json.encodeToString(query)}, $page)")
        else ExtSearchResults(songs = searchSongs(query, page))

    override fun getAlbum(id: String): ExtCollection = executeJs("getAlbum(${json.encodeToString(id)})")
    override fun getArtist(id: String): ExtCollection = executeJs("getArtist(${json.encodeToString(id)})")

    override fun searchSongs(query: String, page: Int): List<ExtSong> {
        val encodedQuery = json.encodeToString(query)
        return executeJs("searchSongs($encodedQuery, $page)")
    }

    override fun getSongDetails(songId: String): ExtSong {
        return executeJs("getSongDetails(${json.encodeToString(songId)})")
    }

    override fun getStreamUrl(songId: String, quality: String): String {
        return executeJs("getStreamUrl(${json.encodeToString(songId)}, ${json.encodeToString(quality)})")
    }
}