package com.aryan.veena.repository.ytmkt

import SongMapper
import com.aryan.veena.repository.datamodels.NowPlayingModel
import dev.toastbits.ytmkt.model.external.ThumbnailProvider.Quality.HIGH
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong

class YtmKTMapper : SongMapper<YtmSong> {
    override fun map(item: YtmSong): NowPlayingModel {

        val songName = item.name
        val artistName = item.artists?.get(0)?.name

        return NowPlayingModel(
            id = item.id,
            url = null,
            name = songName,
            artistName = artistName,
            duration = item.duration,
            imageUrl = item.thumbnail_provider?.getThumbnailUrl(HIGH)
        )
    }
}