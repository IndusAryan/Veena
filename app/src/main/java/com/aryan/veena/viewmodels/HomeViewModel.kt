package com.aryan.veena.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aryan.veena.R
import com.aryan.veena.datamodels.Result
import com.aryan.veena.repository.JioSaavnAPI
import com.aryan.veena.utils.CoroutineUtils.launchIO

class HomeViewModel : ViewModel() {

    private val _searchedSong = MutableLiveData<List<Result>>()
    val searchedSong : LiveData<List<Result>> = _searchedSong

    private val _status = MutableLiveData<Resource<List<Result>>>()
    val status: LiveData<Resource<List<Result>>> = _status

    fun searchSaavn(songName: String) {
        launchIO(this) {
            _status.postValue(Resource.Loading())
            try {
                val result = JioSaavnAPI.retrofitService.getSongs(songName)
                if (result.success) {
                    _searchedSong.postValue(result.data.results)
                    _status.postValue(Resource.Success(result.data.results))
                } else {
                    _status.postValue(Resource.Error(R.string.search_failed))
                    Log.e("JIO_API_RESULT", result.toString())
                }
            }
            catch (t:Throwable) {
                _status.postValue(Resource.Error(R.string.search_failed))
                t.printStackTrace()
            }
        }
    }

    sealed class Resource<T>(val data: T? = null, val message: Int? = null) {
        class Success<T>(data: T) : Resource<T>(data)
        class Error<T>(message: Int, data: T? = null) : Resource<T>(data, message)
        class Loading<T>(data: T? = null) : Resource<T>(data)
    }

    // camera


}