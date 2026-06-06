package com.indus.veena.contract

import kotlinx.serialization.Serializable


// ---------------------------------------------------------------------------
// Wire envelope
// Every value that crosses the JS→Kotlin boundary is wrapped in this type.
// JS always does:  return { ok: true, data: <payload>, error: null }
// or on failure:  return { ok: false, data: null, error: "message" }
// ---------------------------------------------------------------------------

@Serializable
data class ExtensionResult<T>(
    val ok: Boolean,
    val data: T? = null,
    val error: String? = null
)

// ---------------------------------------------------------------------------
// Domain models - these are what JS scripts must produce
// ---------------------------------------------------------------------------

@Serializable
data class ExtSong(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String = "",
    val duration: String = "0",
    val album: String = "",
    val composer: String = "",
    val albumArtist: String? = null,
    val url: String = "",
    val genre: String = "",
    val lyricist: String? = null,
    val year: String = "",
    /** Populated only after getSongDetails() — null means not yet resolved. */
    val streamableUrls: Map<String, String>? = emptyMap()
)

@Serializable
data class ExtAlbum(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnail: String = "",
    val year: String = "",
    val songCount: Int = 0
)

@Serializable
data class ExtPlaylist(
    val id: String,
    val title: String,
    val description: String = "",
    val thumbnail: String = "",
    val songCount: Int = 0
)

@Serializable
data class ExtArtist(
    val id: String,
    val name: String,
    val thumbnail: String = "",
    val bio: String = ""
)

@Serializable
data class ExtSearchResults(
    val songs: List<ExtSong> = emptyList(),
    val albums: List<ExtAlbum> = emptyList(),
    val artists: List<ExtArtist> = emptyList(),
    val playlists: List<ExtPlaylist> = emptyList()
)

// ---------------------------------------------------------------------------
// Strongly-typed errors — never swallow exceptions silently
// ---------------------------------------------------------------------------

sealed class ExtensionError : Exception() {
    data class NetworkError(val url: String, override val message: String) : ExtensionError()
    data class ParseError(val raw: String, override val message: String) : ExtensionError()
    data class ScriptError(val extensionId: String, override val message: String) : ExtensionError()
    data class ManifestError(val reason: String) : ExtensionError()
    data class CapabilityNotSupported(val cap: String, val extensionId: String) : ExtensionError()
    data class PermissionDenied(val permission: String, val extensionId: String) : ExtensionError()
}
