package com.indus.veena.models

data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val size: String,
    val versionCode: Int = 0
)
