package com.aryan.veena.datamodels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Root(
    val success: Boolean,
    val data: Data,
)

@Serializable
data class Data(
    val total: Long,
    val start: Long,
    val results: List<Result>,
)

@Serializable
data class Result(
    val id: String,
    val name: String,
    val type: String,
    val year: String,
    @Contextual
    val releaseDate: Any?,
    val duration: Long,
    val label: String? = null,
    val explicitContent: Boolean,
    val playCount: Long?,
    val language: String,
    val hasLyrics: Boolean,
    @Contextual
    val lyricsId: Any?,
    val url: String,
    val copyright: String,
    val album: Album,
    @Contextual
    val artists: Artists?,
    val image: List<Image3>,
    val downloadUrl: List<DownloadUrl>,
)

@Serializable
@Parcelize
data class Album(
    val id: String,
    val name: String,
    val url: String,
) : Parcelable

@Serializable
data class Artists(
    val primary: List<Primary>,
    //@Contextual val featured: List<Any?>,
    val all: List<All>,
)

@Serializable
data class Primary(
    val id: String,
    val name: String,
    val role: String,
    val image: List<Image>,
    val type: String,
    val url: String,
)

@Serializable
@Parcelize
data class Image( // 50x50 resolution
    val quality: String,
    val url: String,
) : Parcelable

@Serializable
data class All(
    val id: String,
    val name: String,
    val role: String,
    val image: List<Image2>,
    val type: String,
    val url: String,
)

@Serializable
@Parcelize
data class Image2( // 150x150
    val quality: String,
    val url: String,
) : Parcelable

@Serializable
@Parcelize
data class Image3( // 500x500
    val quality: String,
    val url: String,
) : Parcelable

@Serializable
@Parcelize
data class DownloadUrl(
    val quality: String,
    val url: String,
) : Parcelable
