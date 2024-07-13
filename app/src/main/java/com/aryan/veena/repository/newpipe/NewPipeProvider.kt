package com.aryan.veena.repository.newpipe

import com.aryan.veena.repository.MusicProvider
import com.aryan.veena.repository.NowPlayingModel
import com.aryan.veena.repository.Provider
import com.aryan.veena.utils.NewPipeDownloader
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class NewPipeProvider : MusicProvider<StreamInfoItem?>() {

    init {
        NewPipe.init(NewPipeDownloader.getInstance())
        provider = Provider.NEWPIPE
    }

    override suspend fun search(query: String?): List<StreamInfoItem?> {
        val music = YouTube.getSearchExtractor(
            query,
            listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS),
            null
        )
        music.fetchPage()
        // Needs to be casted to StreamableInfoItem, otherwise won't work
        return music.initialPage.items.subList(0, 10) as MutableList<StreamInfoItem?>
    }

    override suspend fun getSong(id: String?): String? {
        TODO("Not yet implemented")
    }

    override fun mapPlayerData(item: StreamInfoItem?): NowPlayingModel {
        val streamableURL = YouTube.getStreamExtractor(item?.url)
        streamableURL.fetchPage()
        streamableURL.audioStreams.sortByDescending { it.bitrate }

        val thumbnailsURL = item?.thumbnails?.last()?.url
        return NowPlayingModel(
            url = streamableURL.audioStreams[0].content,
            name = item?.name,
            artistName = item?.uploaderName,
            duration = item?.duration,
            imageUrl = thumbnailsURL
        )
    }
}