package com.aryan.veena.repository.piped

import com.aryan.veena.repository.MusicProvider
import com.aryan.veena.repository.NowPlayingModel
import com.aryan.veena.repository.Provider
import com.aryan.veena.repository.piped.PipedAPI.fetchPipedAPI
import com.aryan.veena.utils.CoroutineUtils.ioScopeContext
class PipedProvider : MusicProvider<Item?>() {

    init {
        provider = Provider.PIPED
    }

    override suspend fun search(query: String?): List<Item?> {
        val pipedResults = query?.let { fetchPipedAPI.getSearchResults(it, "all").items }
        println(pipedResults)
        return pipedResults as List<Item?>
    }

    override suspend fun getSong(id: String?): String? {
        //var streamingURL: String? = null

          //  try {
              val pipedStream = ////ioScopeContext {
                  fetchPipedAPI.getStreamURL(id!!).audioStreams
             // }

                    val url48Kbps = pipedStream?.find { it?.itag?.toInt() == 139 }?.url // mp4 mp4a
                    val url46kbps = pipedStream?.find { it?.itag?.toInt() == 249 }?.url // webm opus
                    val url58kbps = pipedStream?.find { it?.itag?.toInt() == 250 }?.url // webm opus
                    val url126kbps = pipedStream?.find { it?.itag?.toInt() == 140 }?.url // mp4 m4a
                    val url118kbps =
                        pipedStream?.find { it?.itag?.toInt() == 251 }?.url // webm opus

                    println(pipedStream)
                    println("126KNPS$url126kbps")
                    //streamingURL = url48Kbps
                    println("nonreturnedURL$url46kbps")
                    /* } catch (t: Throwable) {
                t.printStackTrace()
            }*/


                    //println("RETURNEDURL$streamingURL")
            return url48Kbps


    }

    override fun mapPlayerData(item: Item?): NowPlayingModel {
        val imageURL = item?.thumbnail
        val name = item?.title
        val artistName = item?.uploaderName
        val videoID = item?.url?.substringAfter("v=")

        return NowPlayingModel(
            id = videoID,
            url = null,
            name = name,
            artistName = artistName,
            duration = item?.duration,
            imageUrl = imageURL,
            provider = Provider.PIPED,
        )
    }
}