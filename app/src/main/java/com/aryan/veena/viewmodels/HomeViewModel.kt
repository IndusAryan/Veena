package com.aryan.veena.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aryan.veena.R
import com.aryan.veena.repository.MusicProvider
import com.aryan.veena.repository.NowPlayingModel
import com.aryan.veena.repository.Provider
import com.aryan.veena.utils.CoroutineUtils.launchIO
import com.aryan.veena.utils.NewPipeDownloader
import kotlinx.coroutines.Job
import org.schabi.newpipe.extractor.NewPipe

class HomeViewModel : ViewModel() {

    private val _selectedProvider = MutableLiveData<Provider>()
    val selectedProvider: LiveData<Provider> = _selectedProvider

    val providerResults = mutableMapOf<Provider, List<NowPlayingModel>>()

    val _searchedSong = MutableLiveData<List<NowPlayingModel?>>()
    val searchedSong: LiveData<List<NowPlayingModel?>> = _searchedSong

    private val _status = MutableLiveData<Resource<Boolean>>()
    val status: LiveData<Resource<Boolean>> = _status

    private val _searchQuery = MutableLiveData<String?>()
    var searchQuery : LiveData<String?> = _searchQuery

    private var currentSearchJob: Job? = null
    var currentProvider: Provider? = null

    init {
        // Needs to be instantiated once for extractor to work


        // Set default provider to Saavn
        _selectedProvider.value = Provider.SAAVN
        currentProvider = Provider.SAAVN
    }

    fun selectProvider(provider: Provider) {
        _selectedProvider.value = provider
        currentProvider = provider
        provider.let {
            providerResults[provider]?.let {
                _searchedSong.postValue(it)
            } ?: run {
                searchQuery.value?.let { query ->
                    search(query)
                }
            }
        }
    }

    fun search(query: String) {
        _searchQuery.postValue(query)
        val provider = _selectedProvider.value ?: return
        searchProvider(query, provider.musicProvider)
    }

    private fun <T> searchProvider(query: String?, provider: MusicProvider<T>) {
        search(
            query ?: return,
            { provider.search(query) },
            { provider.mapPlayerData(it) }
        )
    }

    private fun <T> search(
        songName: String?,
        fetchSongs: suspend (String?) -> List<T>,
        mapper: (T) -> NowPlayingModel
    ) {
        // Cancel any ongoing search
        if (currentSearchJob != null) {
        currentSearchJob?.cancel() }

        // Start a new search
        currentSearchJob = launchIO(this) {
            setLoading()
            try {
                val items = fetchSongs(songName)
                val nowPlayingModels = items.mapNotNull { mapper(it) }
                Log.d("HomeViewModel", "Mapped NowPlayingModels: $nowPlayingModels")
                if (_selectedProvider.value == currentProvider) {
                    providerResults[currentProvider!!] = nowPlayingModels
                    _searchedSong.postValue(nowPlayingModels)
                    setSuccess()
                }
                setSuccess()
            } catch (t: Throwable) {
                setError()
                handleError(t)
            }
        }
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
}