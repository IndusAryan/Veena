package com.indus.veena.models

data class PlaybackQueue(val source: String, val songs: List<SongModel>, val index: Int) {
    companion object {
        fun from(source: String, songs: List<SongModel>, selected: SongModel): PlaybackQueue {
            val tracks = songs.distinctBy { it.provider to it.id }
            val index = tracks.indexOfFirst { it.provider == selected.provider && it.id == selected.id }
            return if (index >= 0) PlaybackQueue(source, tracks, index)
            else PlaybackQueue(source, tracks + selected, tracks.size)
        }
    }
}
