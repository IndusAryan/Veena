package com.indus.veena.contract

/** Optional request policy supplied by an addon, not inferred from provider names by the app.
 * These methods must be fast and must not extract streams or make network requests.
 * Range for playback is managed by the media engine; download Range can be supplied here.
 */
interface MediaRequestAddon : MusicAddon {
    fun getPlaybackHeaders(songId: String): Map<String, String> = emptyMap()
    fun getDownloadHeaders(songId: String): Map<String, String> = getPlaybackHeaders(songId)
}
