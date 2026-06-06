package com.indus.veena.providers

import android.util.Log
import com.indus.veena.contract.ExtSong
import com.indus.veena.contract.ExtensionHost
import com.indus.veena.contract.MusicAddon
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class NewPipeLocalPlugin : MusicAddon {
    private lateinit var host: ExtensionHost
    private val TAG = "VEENA_NEWPIPE_LOCAL"

    override fun onLoad(host: ExtensionHost) {
        this.host = host
        try {
            NewPipe.init(object : Downloader() {
                override fun execute(request: org.schabi.newpipe.extractor.downloader.Request): Response {
                    val url = request.url()
                    val httpMethod = request.httpMethod()
                    val data = request.dataToSend()
                    return try {
                        val extraHeaders = request.headers()
                            .mapValues { it.value.firstOrNull() ?: "" }

                        val httpResponse = if (httpMethod == "POST") {
                            val bodyStr = data?.let { String(it) } ?: ""
                            host.httpPostFull(
                                url,
                                bodyStr,
                                "application/json",
                                extraHeaders
                            )
                        } else {
                            host.httpGetFull(url, extraHeaders)
                        }

                        if (httpResponse.code != 200) {
                            Log.e(TAG, "NewPipe request got [${httpResponse.code}] for $url")
                        }

                        Response(
                            httpResponse.code,
                            "",
                            httpResponse.headers,
                            httpResponse.body,
                            httpResponse.finalUrl
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "NewPipe downloader failed for $url", e)
                        throw e
                    }
                }
            })
        } catch (e: Exception) {
            Log.w(TAG, "NewPipe init skipped or failed: ${e.message}")
        }
    }

    override fun getSuggestions(query: String): List<String> {
        return try {
            val suggestions = YouTube.suggestionExtractor.suggestionList(query)
            suggestions ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun searchSongs(query: String, page: Int): List<ExtSong> {
        Log.d(TAG, "Searching for: $query")
        return try {
            val extractor = YouTube.getSearchExtractor(
                query, listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), null
            )
            extractor.fetchPage()
            val items = extractor.initialPage.items

            items.filterIsInstance<StreamInfoItem>()
                .take(20)
                .map { item ->
                    ExtSong(
                        id = item.url,
                        title = item.name ?: "Unknown",
                        artist = item.uploaderName ?: "Unknown Artist",
                        thumbnail = item.thumbnails.lastOrNull()?.url?.let { url ->
                            if (url.contains("=")) {
                                url.replaceAfterLast("=", "w720-h720")
                            } else url
                        } ?: "",
                        duration = item.duration.toString(),
                        album = ""
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed", e)
            emptyList()
        }
    }

    override fun getSongDetails(songId: String): ExtSong {
        val absoluteUrl = when {
            songId.startsWith("http") -> songId
            songId.startsWith("/") -> "https://www.youtube.com$songId"
            else -> "https://www.youtube.com/watch?v=$songId"
        }

        Log.d(TAG, "Starting stream extraction process for URL: $absoluteUrl")

        try {
            val extractor = YouTube.getStreamExtractor(absoluteUrl)
            Log.d(TAG, "Extractor initialized. Fetching page layout...")
            extractor.fetchPage()
            Log.d(TAG, "Page layout fetch complete.")

            val audioStreams = extractor.audioStreams
            Log.d(TAG, "Total audio streams found: ${audioStreams.size}")
            audioStreams.forEachIndexed { index, stream ->
                Log.d(TAG, "  Audio Stream [$index]: Format=${stream.format?.suffix}, Bitrate=${stream.bitrate}, URL=${stream.content}")
            }

            val videoStreams = extractor.videoStreams
            Log.d(TAG, "Total video streams found: ${videoStreams.size}")
            videoStreams.forEachIndexed { index, stream ->
                Log.d(TAG, "  Video Stream [$index]: Format=${stream.format?.suffix}, Resolution=${stream.resolution}, URL=${stream.content}")
            }

            val sortedAudio = audioStreams.sortedByDescending { it.bitrate }
            val playableURLs = mutableMapOf<String, String>()

            if (sortedAudio.isNotEmpty()) {
                playableURLs["320kbps"] = sortedAudio.first().content
                playableURLs["160kbps"] = sortedAudio.first().content
                if (sortedAudio.size > 1) {
                    playableURLs["128kbps"] = sortedAudio[sortedAudio.size / 2].content
                }
                playableURLs["48kbps"] = sortedAudio.last().content
                Log.d(TAG, "Successfully populated adaptive audio streams.")
            } else if (videoStreams.isNotEmpty()) {
                Log.w(TAG, "No adaptive audio streams returned. Deploying progressive video fallback pipeline...")
                val fallbackUrl = videoStreams.first().content
                playableURLs["320kbps"] = fallbackUrl
                playableURLs["160kbps"] = fallbackUrl
                playableURLs["128kbps"] = fallbackUrl
                playableURLs["48kbps"] = fallbackUrl
            } else {
                Log.w(TAG, "Warning: Extractor succeeded, but both audio and video streams are empty.")
            }

            val rawDescription = extractor.description?.content ?: ""
            val composer = Regex("Composer:\\s*(.*)").find(rawDescription)?.groupValues?.get(1)?.trim() ?: ""
            val lyricist = Regex("Lyricist:\\s*(.*)").find(rawDescription)?.groupValues?.get(1)?.trim() ?: ""
            var parsedYear = ""

            val releasedOnMatch = Regex("Released on:\\s*(\\d{4})").find(rawDescription)
            if (releasedOnMatch != null) {
                parsedYear = releasedOnMatch.groupValues[1]
            }

            if (parsedYear.isEmpty()) {
                parsedYear = extractor.uploadDate?.instant?.toString()?.take(4) ?: ""
            }
            var parsedAlbum = ""
            val blocks = rawDescription.split("<br>").map { it.trim() }.filter { it.isNotEmpty() }
            blocks.forEachIndexed { index, block ->
                if (block.contains("·") && index + 1 < blocks.size) {
                    val next = blocks[index + 1]
                    if (!next.startsWith("℗") && !next.startsWith("Released") && !next.startsWith("Composer")) {
                        parsedAlbum = next
                    }
                }
            }

            return ExtSong(
                id = extractor.url,
                title = extractor.name ?: "Unknown Title",
                artist = (extractor.uploaderName ?: "Unknown Artist").removeSuffix(" - Topic"),
                thumbnail = extractor.uploaderAvatars?.firstOrNull()?.url?.replace("s48", "s720") ?: "",
                duration = extractor.length.toString(),
                streamableUrls = playableURLs,
                album = parsedAlbum,
                year = parsedYear,
                composer = composer,
                lyricist = lyricist,
                genre = extractor.category ?: ""
            )

        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL FAILURE: getSongDetails extraction crashed for ID: $songId", e)
            throw e
        }
    }

    override fun getStreamUrl(songId: String, quality: String): String {
        val details = getSongDetails(songId)
        return details.streamableUrls?.get(quality) ?: ""
    }
}