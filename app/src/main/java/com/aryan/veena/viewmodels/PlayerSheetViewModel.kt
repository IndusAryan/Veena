package com.aryan.veena.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class PlayerSheetViewModel(application: Application) : AndroidViewModel(application) {

    private val player: ExoPlayer = ExoPlayer.Builder(application).build()
    val playerLiveData = MutableLiveData(player)
    private var currentPosition: Long = 0

    fun play(url: String?) {
        url?.let {
            val mediaItem = MediaItem.Builder()
                .setMediaId("media-1")
                .setUri(it)
                .build()
            player.apply {
                setMediaItem(mediaItem)
                prepare()
                seekTo(currentPosition)
                playWhenReady = true
            }
        }
    }

    fun pause() {
        player.playWhenReady = false
    }

    fun resume() {
        player.playWhenReady = true
    }

    fun isPlaying(): Boolean = player.isPlaying

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun releasePlayer() {
        player.release()
    }

    fun saveCurrentPosition() {
        currentPosition = player.currentPosition
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}