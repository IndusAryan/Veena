package com.aryan.veena.repository.jiosaavn

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Root(
    val success: Boolean,
    val data: Data,
) : Parcelable

@Parcelize
@Serializable
data class Data(
    val total: Long,
    val start: Long,
    val results: List<Result>,
) : Parcelable

@Parcelize
@Serializable
data class Result(
    val id: String,
    val name: String,
    val type: String,
    val year: String,
    val releaseDate: CharSequence?, // was Any
    val duration: Long,
    val label: String? = null,
    val explicitContent: Boolean,
    val playCount: Long?,
    val language: String,
    val hasLyrics: Boolean,
    val lyricsId: CharSequence?, // was Any
    val url: String,
    val copyright: String,
    val album: Album,
    val artists: Artists?,
    val image: List<Image3>,
    val downloadUrl: List<DownloadUrl>,
) : Parcelable

@Parcelize
@Serializable
data class Album(
    val id: String,
    val name: String,
    val url: String,
) : Parcelable

@Parcelize
@Serializable
data class Artists(
    val primary: List<Primary>,
    //@Contextual val featured: List<Any?>,
    val all: List<All>,
) : Parcelable

@Parcelize
@Serializable
data class Primary(
    val id: String,
    val name: String,
    val role: String,
    val image: List<Image>,
    val type: String,
    val url: String,
) : Parcelable

@Parcelize
@Serializable
data class Image( // 50x50 resolution
    val quality: String,
    val url: String,
) : Parcelable

@Parcelize
@Serializable
data class All(
    val id: String,
    val name: String,
    val role: String,
    val image: List<Image2>,
    val type: String,
    val url: String,
) : Parcelable

@Parcelize
@Serializable
data class Image2( // 150x150
    val quality: String,
    val url: String,
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
    val quality: String,
    val url: String,
) : Parcelable
