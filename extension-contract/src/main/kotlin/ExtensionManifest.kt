package com.indus.veena.contract

import kotlinx.serialization.Serializable

// Loader-only — never exposed outside ExtensionManager
enum class ExtensionType { JS, DEX }

/**
 * Every JS extension file MUST declare this as its first statement:
 *
 *   const MANIFEST = {
 *     id: "saavn",
 *     name: "Saavn",
 *     version: "1.0.0",
 *     apiVersion: 1,
 *     capabilities: ["search", "details", "stream"],
 *     permissions: ["network"],
 *     author: "yourname",
 *     description: "JioSaavn music source"
 *   };
 *
 * Kotlin reads this before executing the full script. If the manifest is
 * missing, malformed, or declares an incompatible apiVersion, the extension
 * is rejected and never loaded into the registry.
 */
@Serializable
data class ExtensionManifest(
    val id: String,
    val name: String,
    val version: String,           // semver string e.g. "1.2.0"
    val versionCode: Int,          // integer for update comparison
    val apiVersion: Int,
    val capabilities: List<String> = emptyList(),
    val description: String = "No description provided.",
    val author: String = "Unknown",
    val homepage: String = "",
    val iconUrl: String = "",
    val updateUrl: String = "",    // where to check for newer .veena/.js
    val entryPoint: String = ""    // DEX only — developer-authored, validated at build time
) {
    companion object {
        const val CURRENT_API_VERSION = 1
    }
    fun supports(cap: String) = cap in capabilities
    fun isNewerThan(other: ExtensionManifest) = versionCode > other.versionCode
}

// Runtime-loaded extension — type is known by loader, not the plugin
data class LoadedExtension(
    val addon: MusicAddon,
    val manifest: ExtensionManifest,
    val type: ExtensionType
)