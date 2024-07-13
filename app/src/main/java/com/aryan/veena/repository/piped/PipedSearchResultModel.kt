package com.aryan.veena.repository.piped

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class SearchRoot(
    val items: List<Item>? = null,
    val nextpage: String? = null,
    val suggestion: String? = null,
    val corrected: Boolean? = null,
) : Parcelable

@Serializable
@Parcelize
data class Item(
    val url: String? = null,
    val type: String? = null,
    val title: String? = null,
    val thumbnail: String? = null,
    val uploaderName: String? = null,
    val uploaderUrl: String? = null,
    val uploaderAvatar: String? = null,
    val uploadedDate: String? = null,
    val shortDescription: String? = null,
    val duration: Long? = null,
    val views: Long? = null,
    val uploaded: Long? = null,
    val uploaderVerified: Boolean? = null,
    val isShort: Boolean? = null,
) : Parcelable

