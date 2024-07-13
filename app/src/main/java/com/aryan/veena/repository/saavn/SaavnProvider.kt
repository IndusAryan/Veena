package com.aryan.veena.repository.saavn

import android.util.Log
import com.aryan.veena.repository.MusicProvider
import com.aryan.veena.repository.NowPlayingModel
import com.aryan.veena.repository.Provider
import com.aryan.veena.repository.saavn.SaavnAPI.fetchSaavnAPI
import com.aryan.veena.utils.CoroutineUtils.ioScopeContext

class SaavnProvider : MusicProvider<Result>() {

    init {
        provider = Provider.SAAVN
    }

    override suspend fun search(query: String?): List<Result> {
        return ioScopeContext {
        val saavnSearchResults = fetchSaavnAPI.getSongs(query!!).data?.results
        Log.d("SaavnProvider", "Search results: $saavnSearchResults")
        return@ioScopeContext saavnSearchResults!!
        }
    }

    override suspend fun getSong(id: String?): String? {
        throw NotImplementedError()
    }

    override fun mapPlayerData(item: Result?): NowPlayingModel {
        val imageUrl = item?.image?.find { it?.quality == "500x500" }?.url
        val artistName = item?.artists?.primary?.getOrNull(0)?.name
        val downloadUrl = item?.downloadUrl?.find { it?.quality == "96kbps" }?.url
        //val downloadUrl = item?.downloadUrl?.find { it?.quality == "12kbps" }?.url

        // some names contains this in their name, so we filter it right here
        val songName = item?.name?.replace("&quot;", "")

        Log.d("SaavnProvider", "Mapping result to NowPlayingModel: $item")

        return NowPlayingModel(
            url = downloadUrl,
            name = songName,
            artistName = artistName,
            duration = item?.duration,
            imageUrl = imageUrl,
            provider = Provider.SAAVN,
        )
    }
}