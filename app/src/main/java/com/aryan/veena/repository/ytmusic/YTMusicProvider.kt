package com.aryan.veena.repository.ytmusic

import com.aryan.veena.repository.MusicProvider
import com.aryan.veena.repository.NowPlayingModel
import com.aryan.veena.repository.Provider
import com.aryan.veena.utils.CoroutineUtils.ioScopeContext
import dev.toastbits.ytmkt.endpoint.SearchResults
import dev.toastbits.ytmkt.formats.VideoFormatsEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.model.external.ThumbnailProvider
import dev.toastbits.ytmkt.model.external.mediaitem.YtmSong
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

class YTMusicProvider : MusicProvider<YtmSong?>() {

    init {
        // needed due to ktor fuc*ery otherwise ytm doesn't work
        val client = HttpClient(CIO)
        provider = Provider.YTMUSIC
    }

    override suspend fun search(query: String?): List<YtmSong?> {

            val searchEndpoint = api.Search
            val searchResult: SearchResults? =
                query?.let { searchEndpoint.search(it, SONG_FILTER, false).getOrThrow() }
            val songs = searchResult?.categories?.flatMap { category ->
                category.first.items.filterIsInstance<YtmSong?>()
            }
            println(songs)
            return songs ?: return emptyList()
    }

    override suspend fun getSong(id: String?): String? {
        var streamableURL : String? = null
        val videoEndpoint: VideoFormatsEndpoint = YoutubeiApi().VideoFormats

        try {
            ioScopeContext {
                val video = id?.let { videoEndpoint.getVideoFormats(it).getOrThrow() }

                val url48Kbps = video?.find { it.itag == 139 }?.url // mp4 mp4a
                val url53kbps = video?.find { it.itag == 249 }?.url // webm opus
                val url69kbps = video?.find { it.itag == 250 }?.url // webm opus
                val url126kbps = video?.find { it.itag == 140 }?.url // mp4 m4a
                val url133kbps = video?.find { it.itag == 251 }?.url // webm opus

                //TODO Return all qualities when user preference is set

                println("48KBPS $url48Kbps")
                val videoInfo = video?.forEach { it ->
                    println(it.url)
                    println(it.itag)
                    println(it.audioTrack)
                    println(it.audioTrackType)
                    println(it.bitrate)
                    println(it.mimeType)
                }

                streamableURL = url133kbps
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        println(streamableURL)
        return streamableURL
    }

    override fun mapPlayerData(item: YtmSong?): NowPlayingModel {
        val songName = item?.name
        val artistName = item?.artists?.get(0)?.name

        return NowPlayingModel(
            id = item?.id,
            url = null,
            name = songName,
            artistName = artistName,
            duration = null,
            imageUrl = item?.thumbnail_provider?.getThumbnailUrl(ThumbnailProvider.Quality.HIGH),
            provider = Provider.YTMUSIC,
        )

    }

    companion object YouTubeMusicConstants {
        const val SONG_FILTER = "EgWKAQIIAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
        val api = YoutubeiApi(data_language = "en-GB")
    }
}