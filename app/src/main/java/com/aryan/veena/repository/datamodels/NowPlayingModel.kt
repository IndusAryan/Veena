package com.aryan.veena.repository.datamodels

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NowPlayingModel(
    val id : String? = null, // when a provider does not provide direct streamable url, and needs another api call with song id
    val url: String? = null,
    val name: String? = null,
    val artistName: String? = null,
    val duration: Long? = null,
    val imageUrl: String? = null
) : Parcelable
