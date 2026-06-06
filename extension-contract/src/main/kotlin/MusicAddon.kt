package com.indus.veena.contract

/**
 * The contract every loaded extension must satisfy.
 *
 * Methods guard themselves: if the extension's manifest doesn't declare
 * the matching capability, they throw [ExtensionError.CapabilityNotSupported]
 * rather than calling into JS and getting a cryptic "not a function" error.
 *
 * All methods are suspend — callers never need to know whether a result
 * came from a network call, a cache, or a local file.
 */
interface MusicAddon {

    /**
     * @param query  user-entered search string
     * @param page   1-based page index
     * @throws ExtensionError.CapabilityNotSupported if manifest lacks "search"
     * @throws ExtensionError.ScriptError on JS runtime failure
     * @throws ExtensionError.NetworkError on HTTP failure
     */
    fun searchSongs(query: String, page: Int = 1): List<ExtSong>

    /** Full search across all entity types — optional, default impl delegates to searchSongs. */
    fun search(query: String, page: Int = 1): ExtSearchResults =
        ExtSearchResults(songs = searchSongs(query, page))

    fun getSuggestions(query: String): List<String> = emptyList()

    /**
     * Fetches full song details including [ExtSong.streamableUrls].
     * @throws ExtensionError.CapabilityNotSupported if manifest lacks "details"
     */
    fun getSongDetails(songId: String): ExtSong

    /**
     * Returns a resolved stream URL for the given quality key.
     * Quality keys are extension-defined (e.g. "320kbps", "160kbps").
     * @throws ExtensionError.CapabilityNotSupported if manifest lacks "stream"
     */
    fun getStreamUrl(songId: String, quality: String = "320kbps"): String

    /** Called once after the extension is loaded. Implementations may pre-warm caches. */
    fun onLoad(host: ExtensionHost) {}

    /** Called before the extension is unloaded/replaced. Release any held resources. */
    fun onUnload() {}
}

data class HttpResponse(val code: Int, val headers: Map<String, List<String>>, val body: String, val finalUrl: String)

/**
 * Access bridge provided to extensions (JS/DEX) to run network, cryptography,
 * and key-value storage operations uniformly.
 */
interface ExtensionHost {
    fun httpGet(url: String, headers: Map<String, String> = emptyMap()): String
    fun httpGetFull(url: String, headers: Map<String, String> = emptyMap()): HttpResponse
    fun httpPost(url: String, body: String, contentType: String = "application/json", headers: Map<String, String> = emptyMap()): String
    fun httpPostFull(
        url: String, body: String, contentType: String = "application/json",
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    fun decrypt(payload: String, extractor: String): String
    fun storageGet(extensionId: String, key: String): String?
    fun storageSet(extensionId: String, key: String, value: String)
    fun storageRemove(extensionId: String, key: String)
    fun <T> runSuspending(block: suspend () -> T): T  // for blocking bridge (NewPipe Downloader etc)
    suspend fun <T> runOnIO(block: suspend () -> T): T  // for suspend callers on IO
    suspend fun <T> runOnMain(block: suspend () -> T): T  // for suspend callers on Main
}