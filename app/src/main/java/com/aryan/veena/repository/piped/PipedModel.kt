package com.aryan.veena.repository.piped

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class PipedModel(
    val root: Root
) : Parcelable

@Serializable
@Parcelize
data class Root(
    val title: String,
    val description: String,
    val uploadDate: String,
    val uploader: String,
    val uploaderUrl: String,
    val uploaderAvatar: String,
    val thumbnailUrl: String,
    val hls: String,
    //@Contextual
    //val dash: Any?,
    //@Contextual val lbryId: Any?,
    val category: String,
    val license: String,
    val visibility: String,
    val tags: List<String>,
    //@Contextual val metaInfo: List<Any?>,
    val uploaderVerified: Boolean,
    val duration: Long,
    val views: Long,
    val likes: Long,
    val dislikes: Long,
    val uploaderSubscriberCount: Long,
    val uploaded: Long,
    val audioStreams: List<AudioStream>? = null,
    val videoStreams: List<VideoStream>,
    val relatedStreams: List<RelatedStream>,
    //@Contextual val subtitles: List<Any?>,
    val livestream: Boolean,
    val proxyUrl: String,
    //val chapters: List<Any?>,
    val previewFrames: List<PreviewFrame>,
) : Parcelable

@Serializable
@Parcelize
data class AudioStream(
    val url: String,
    val format: String,
    val quality: String,
    val mimeType: String,
    val codec: String,
    /*@Contextual val audioTrackId: Any? = null,
    @Contextual val audioTrackName: Any? = null,
    @Contextual val audioTrackType: Any? = null,
    @Contextual val audioTrackLocale: Any? = null,*/
    val videoOnly: Boolean,
    val itag: Long,
    val bitrate: Long,
    val initStart: Long,
    val initEnd: Long,
    val indexStart: Long,
    val indexEnd: Long,
    val width: Long,
    val height: Long,
    val fps: Long,
    val contentLength: Long,
) : Parcelable

@Serializable
@Parcelize
data class VideoStream(
    val url: String,
    val format: String,
    val quality: String,
    val mimeType: String,
    val codec: String?,
    //val audioTrackId: Any?,
    //val audioTrackName: Any?,
    //val audioTrackType: Any?,
    //val audioTrackLocale: Any?,
    val videoOnly: Boolean,
    val itag: Long,
    val bitrate: Long,
    val initStart: Long,
    val initEnd: Long,
    val indexStart: Long,
    val indexEnd: Long,
    val width: Long,
    val height: Long,
    val fps: Long,
    val contentLength: Long,
) : Parcelable

@Serializable
@Parcelize
data class RelatedStream(
    val url: String,
    val type: String,
    val title: String?,
    val thumbnail: String,
    val uploaderName: String,
    val uploaderUrl: String?,
    val uploaderAvatar: String?,
    val uploadedDate: String?,
    //val shortDescription: Any?,
    val duration: Long?,
    val views: Long?,
    val uploaded: Long?,
    val uploaderVerified: Boolean,
    val isShort: Boolean?,
    val name: String?,
    val playlistType: String?,
    val videos: Long?,
) : Parcelable

@Serializable
@Parcelize
data class PreviewFrame(
    val urls: List<String>,
    val frameWidth: Long,
    val frameHeight: Long,
    val totalCount: Long,
    val durationPerFrame: Long,
    val framesPerPageX: Long,
    val framesPerPageY: Long,
) : Parcelable