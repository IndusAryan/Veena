package com.aryan.veena.viewmodels

import SongMapper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aryan.veena.R
import com.aryan.veena.repository.datamodels.NowPlayingModel
import com.aryan.veena.repository.jiosaavn.JioSaavnAPI
import com.aryan.veena.repository.jiosaavn.JioSaavnMapper
import com.aryan.veena.repository.youtube.YouTubeMapper
import com.aryan.veena.repository.ytmkt.YtmKTMapper
import com.aryan.veena.repository.ytmkt.YtmSong
import com.aryan.veena.utils.CoroutineUtils.launchIO
import com.aryan.veena.utils.NewPipeDownloader
import dev.toastbits.ytmkt.endpoint.SearchEndpoint
import dev.toastbits.ytmkt.endpoint.SearchResults
import dev.toastbits.ytmkt.endpoint.SearchType
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class HomeViewModel : ViewModel() {

    private val _selectedProvider = MutableLiveData<Provider>()
    val selectedProvider: LiveData<Provider> = _selectedProvider

    private val _searchedSong = MutableLiveData<List<NowPlayingModel>>()
    val searchedSong: LiveData<List<NowPlayingModel>> = _searchedSong

    private val _status = MutableLiveData<Resource<Boolean>>()
    val status: LiveData<Resource<Boolean>> = _status

    private val _searchQuery = MutableLiveData<String>()
    var searchQuery : LiveData<String> = _searchQuery

    private var saavnResults: List<NowPlayingModel>? = null
    private var ytResults: List<NowPlayingModel>? = null

    init {
        // Needs to be instantiated once for extractor to work
        NewPipe.init(NewPipeDownloader.getInstance())
        // Set default provider to JioSaavn
        _selectedProvider.value = Provider.JIO_SAAVN
    }

    fun selectProvider(provider: Provider) {
        _selectedProvider.value = provider
        when (provider) {
            Provider.JIO_SAAVN -> saavnResults?.let { _searchedSong.postValue(it) }
            Provider.YOUTUBE -> ytResults?.let { _searchedSong.postValue(it) }
        }
    }

    fun search(query: String) {
        _searchQuery.postValue(query)
        when (_selectedProvider.value) {
            Provider.JIO_SAAVN -> searchSaavn(query)
            Provider.YOUTUBE ->  searchYTMKT(query) //searchYTM(query)
            else -> {}
        }
    }

    private fun searchSaavn(songName: String) {
        search(
            songName,
            { JioSaavnAPI.retrofitService.getSongs(songName).data.results },
            JioSaavnMapper()
        )
    }

    private fun searchYTM(songName: String) {
        search(
            songName,
            {
                val music = YouTube.getSearchExtractor(
                    songName,
                    listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS),
                    null
                )
                music.fetchPage()
                // Needs to be casted to StreamableInfoItem, otherwise won't work
                music.initialPage.items.subList(0, 5) as MutableList<StreamInfoItem>
            },
            YouTubeMapper()
        )
    }

    private fun searchYTMKT(songName: String) {
        search(songName, {
            val api = YoutubeiApi(
                data_language = "en-GB"
            )
            val endpoint: SearchEndpoint = api.Search
            val searchResult: SearchResults = endpoint.search(songName, SONG_FILTER, false).getOrThrow()
            val songs = searchResult.categories.flatMap { category ->
                category.first.items.filterIsInstance<dev.toastbits.ytmkt.model.external.mediaitem.YtmSong>()
            }
            println(songs)
            return@search songs

        },
            YtmKTMapper()
        )
    }

    private fun <T> search(
        songName: String,
        fetchSongs: suspend (String) -> List<T>,
        mapper: SongMapper<T>
    ) {
        launchIO(this) {
            setLoading()
            try {
                val items = fetchSongs(songName)
                val nowPlayingModels = items.map { mapper.map(it) }
                _searchedSong.postValue(nowPlayingModels)
                setSuccess()
            } catch (t: Throwable) {
                setError()
                handleError(t)
            }
        }
    }

    enum class Provider {
        JIO_SAAVN, YOUTUBE
    }

    private fun setLoading() {
        _status.postValue(Resource.Loading())
    }

    private fun setSuccess() {
        _status.postValue(Resource.Success())
    }

    private fun setError() {
        _status.postValue(Resource.Error(R.string.search_failed))
    }

    private fun handleError(t: Throwable) {
        _status.postValue(Resource.Error(R.string.search_failed))
        t.printStackTrace()
    }

    sealed class Resource<T>(val data: T? = null, val message: Int? = null) {
        class Success<T> : Resource<T>()
        class Error<T>(message: Int, data: T? = null) : Resource<T>(data, message)
        class Loading<T>(data: T? = null) : Resource<T>(data)
    }

    companion object YouTubeMusicConstants {
        val SONG_FILTER = "EgWKAQIIAUICCAFqDBAOEAoQAxAEEAkQBQ%3D%3D"
    }
}