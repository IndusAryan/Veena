package com.indus.veena.ui.screens.downloads


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indus.veena.di.DownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadManager: DownloadManager
) : ViewModel() {

    val downloads = downloadManager.activeDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePause(songId: String, isDownloading: Boolean) {
        viewModelScope.launch {
            if (isDownloading) downloadManager.pauseDownload(songId)
            else downloadManager.resumeDownload(songId)
        }
    }

    fun removeDownload(songId: String) {
        viewModelScope.launch {
            downloadManager.removeDownload(songId)
        }
    }
}