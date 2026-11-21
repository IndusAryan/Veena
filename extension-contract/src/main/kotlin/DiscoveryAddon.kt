package com.indus.veena.contract

import kotlinx.serialization.Serializable

/** Optional capability. Old addons remain loadable without implementing discovery.
 * Declare "discovery" in the manifest and return provider-curated music sections.
 * Blocking network/extraction work is dispatched to IO by the host.
 */
interface DiscoveryAddon : MusicAddon {
    fun discover(): List<ExtDiscoverySection>
}

@Serializable
data class ExtDiscoverySection(
    val id: String,
    val title: String,
    val songs: List<ExtSong>,
    val subtitle: String = ""
)
