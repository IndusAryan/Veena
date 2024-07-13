package com.aryan.veena.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DownloadsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "⚠ Library and History is currently work in progress :)"
    }
    val text: LiveData<String> = _text
}