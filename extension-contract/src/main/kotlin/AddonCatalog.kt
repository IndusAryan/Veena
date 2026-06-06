package com.indus.veena.contract

import kotlinx.serialization.Serializable

/**
 * Central index served from the veena-extensions catalog repository.
 * The app fetches this file to discover and download community addons.
 */
@Serializable
data class AddonCatalog(
    val catalogVersion: Int = 1,
    val updatedAt: String = "",
    val addons: List<AddonCatalogEntry> = emptyList()
)

/**
 * Metadata for a community addon hosted in its own repository.
 *
 * [manifestUrl] points to the author's `manifest.json` for version checks.
 * [downloadUrl] is the direct URL to the latest `.veena` release asset.
 */
@Serializable
data class AddonCatalogEntry(
    val id: String,
    val name: String,
    val description: String = "",
    val author: String = "Unknown",
    val iconUrl: String = "",
    val capabilities: List<String> = emptyList(),
    val repo: String = "",
    val manifestUrl: String = "",
    val downloadUrl: String = "",
    val sha256: String = "",
    val minApiVersion: Int = 1,
    val nsfw: Boolean = false
)
