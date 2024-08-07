package com.aryan.veena.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aryan.veena.databinding.SongItemBinding
import com.aryan.veena.repository.NowPlayingModel
import com.aryan.veena.ui.viewholders.SongViewHolder

class SongsAdapter(private var songs: List<NowPlayingModel>) :
    RecyclerView.Adapter<SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SongItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        if (song != null) {
            holder.bind(song)
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun updateSongs(newSongs: List<NowPlayingModel>) {
        Log.d("SongsAdapter", "Updating songs: $newSongs")
        songs = newSongs
        notifyDataSetChanged()
    }
}
