package com.aryan.veena.repository.youtube

import SongMapper
import com.aryan.veena.repository.datamodels.NowPlayingModel
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class YouTubeMapper : SongMapper<StreamInfoItem> {
    override fun map(item: StreamInfoItem): NowPlayingModel {

        val streamableURL = YouTube.getStreamExtractor(item.url)
        streamableURL.fetchPage()
        streamableURL.audioStreams.sortByDescending { it.bitrate }

        val thumbnailsURL = item.thumbnails.last()?.url
        return NowPlayingModel(
            url = streamableURL.audioStreams[0].content,
            name = item.name,
            artistName = item.uploaderName,
            duration = item.duration,
            imageUrl = thumbnailsURL
        )
    }
}