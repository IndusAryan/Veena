package com.aryan.veena.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aryan.veena.databinding.SongItemBinding
import com.aryan.veena.datamodels.Result
import com.aryan.veena.ui.viewholders.SongViewHolder

class SongsAdapter(private var songs: List<Result>) :
    RecyclerView.Adapter<SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SongItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun updateSongs(newSongs: List<Result>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
