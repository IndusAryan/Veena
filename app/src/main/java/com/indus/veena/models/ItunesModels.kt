package com.indus.veena.models

import kotlinx.serialization.Serializable

@Serializable
data class ItunesSearchResponse(
    val resultCount: Int,
    val results: List<ItunesSearchResult> = emptyList()
)

@Serializable
data class ItunesSearchResult(
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionName: String? = null
)
