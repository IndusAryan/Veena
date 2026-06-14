package com.indus.veena.models

import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    val tag_name: String,
    val assets: List<GithubAsset>
)

@Serializable
data class GithubAsset(
    val name: String,
    val size: Long,
    val browser_download_url: String
)

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val size: String
)
