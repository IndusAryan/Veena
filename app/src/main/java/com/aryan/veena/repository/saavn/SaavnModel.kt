package com.aryan.veena.repository.saavn

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Root(
    val success: Boolean? = null,
    val data: Data? = null
) : Parcelable

@Parcelize
@Serializable
data class Data(
    val total: Long? = null,
    val start: Long? = null,
    val results: List<Result>
) : Parcelable

@Parcelize
@Serializable
data class Result(
    val id: String? = null,
    val name: String? = null,
    val type: String? = null,
    val year: String? = null,
    val releaseDate: CharSequence?,
    val duration: Long? = null,
    val label: String? = null,
    val explicitContent: Boolean? = null,
    val playCount: Long? = null,
    val language: String? = null,
    val hasLyrics: Boolean? = null,
    val lyricsId: CharSequence?,
    val url: String? = null,
    val copyright: String? = null,
    val album: Album? = null,
    val artists: Artists? = null,
    val image: List<Image3?>? = null,
    val downloadUrl: List<DownloadUrl?>? = null
) : Parcelable

@Parcelize
@Serializable
data class Album(
    val id: String? = null,
    val name: String? = null,
    val url: String? = null
) : Parcelable

@Parcelize
@Serializable
data class Artists(
    val primary: List<Primary?>? = null,
    //val featured: List<Any?>,  // Remains non-nullable due to annotation limitations
    val all: List<All?>? = null
) : Parcelable

@Parcelize
@Serializable
data class Primary(
    val id: String? = null,
    val name: String? = null,
    val role: String? = null,
    val image: List<Image?>? = null,
    val type: String? = null,
    val url: String? = null
) : Parcelable

@Parcelize
@Serializable
data class Image(
    val quality: String? = null,
    val url: String? = null
) : Parcelable

@Parcelize
@Serializable
data class All(
    val id: String? = null,
    val name: String? = null,
    val role: String? = null,
    val image: List<Image2?>? = null,
    val type: String? = null,
    val url: String? = null
) : Parcelable

@Parcelize
@Serializable
data class Image2(
    val quality: String? = null,
    val url: String? = null
) : Parcelable

@Parcelize
@Serializable
data class Image3( // 500x500
    val quality: String,
    val url: String,
) : Parcelable

@Parcelize
@Serializable
data class DownloadUrl(
    val quality: String? = null,
    val url: String? = null
) : Parcelable

