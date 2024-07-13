package com.aryan.veena.repository.piped

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class PipedModel(
    val title: String? = null,
    val description: String? = null,
    val uploadDate: String? = null,
    val uploader: String? = null,
    val uploaderUrl: String? = null,
    val uploaderAvatar: String? = null,
    val thumbnailUrl: String? = null,
    val hls: String? = null,
    val category: String? = null,
    val license: String? = null,
    val visibility: String? = null,
    val tags: List<String?>? = null, // nullable list of nullable strings
    val uploaderVerified: Boolean? = null,
    val duration: Long? = null,
    val views: Long? = null,
    val likes: Long? = null,
    val dislikes: Long? = null,
    val uploaderSubscriberCount: Long? = null,
    val uploaded: Long? = null,
    val audioStreams: List<AudioStream?>? = null,
    //val subtitles: List<Any?>? = null,
    val livestream: Boolean? = null,
    val proxyUrl: String? = null,
    //val chapters: List<Any?>? = null,
) : Parcelable

@Serializable
@Parcelize
data class AudioStream(
    val url: String? = null,
    val format: String? = null,
    val quality: String? = null,
    val mimeType: String? = null,
    val codec: String? = null,
    /*val audioTrackId: Any? = null,
    val audioTrackName: Any? = null,
    val audioTrackType: Any? = null,
    val audioTrackLocale: Any? = null, */
    val videoOnly: Boolean? = null,
    val itag: Long? = null,
    val bitrate: Long? = null,
    val initStart: Long? = null,
    val initEnd: Long? = null,
    val indexStart: Long? = null,
    val indexEnd: Long? = null,
    val width: Long? = null,
    val height: Long? = null,
    val fps: Long? = null,
    val contentLength: Long? = null,
) : Parcelable


