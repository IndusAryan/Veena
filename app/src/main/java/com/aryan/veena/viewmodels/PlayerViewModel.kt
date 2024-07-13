package com.aryan.veena.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayerViewModel() : ViewModel() {

    private val _isMediaSessionServiceClosed = MutableLiveData<Boolean>()
    val isMediaServiceClosed : LiveData<Boolean> get() = _isMediaSessionServiceClosed

    fun closeMediaService() {
        _isMediaSessionServiceClosed.value = true
    }
    override fun onCleared() {
        super.onCleared()
    }
}