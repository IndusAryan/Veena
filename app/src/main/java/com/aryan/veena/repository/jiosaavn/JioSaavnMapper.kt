package com.aryan.veena.repository.jiosaavn

import SongMapper
import com.aryan.veena.repository.datamodels.NowPlayingModel

class JioSaavnMapper : SongMapper<Result> {
    override fun map(item: Result): NowPlayingModel {
        val imageUrl = item.image.find { it.quality == "500x500" }?.url
        val artistName = item.artists?.primary?.getOrNull(0)?.name
        //val downloadUrl = item.downloadUrl.find { it.quality == "96kbps" }?.url
        val downloadUrl = item.downloadUrl.find { it.quality == "12kbps" }?.url

        return NowPlayingModel(
            url = downloadUrl ?: "",
            name = item.name,
            artistName = artistName,
            duration = item.duration,
            imageUrl = imageUrl
        )
    }
}