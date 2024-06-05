package com.aryan.veena.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _scannedText = MutableLiveData<String>()
    val scannedText: LiveData<String> get() = _scannedText

    fun setScannedText(text: String) {
        _scannedText.value = text
    }
}