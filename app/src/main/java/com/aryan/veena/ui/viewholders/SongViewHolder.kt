package com.aryan.veena.ui.viewholders

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.aryan.veena.R
import com.aryan.veena.databinding.SongItemBinding
import com.aryan.veena.datamodels.Result
import com.aryan.veena.ui.fragments.PlayerSheetFragment
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SongViewHolder(private val binding: SongItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(song: Result) {
        val imageToLoad = song.image.find { it.quality == "500x500" }
        val downloadURl96kbps = song.downloadUrl.find { it.quality == "96kbps" }?.url.toString()
        val artists = song.artists?.primary?.getOrNull(0)?.name

        binding.songName.text = song.name
        binding.artistName.text = artists
        binding.songDuration.text = song.duration.toDuration(DurationUnit.SECONDS).toString()

        binding.songImage.load(imageToLoad?.url) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_help)
            error(R.drawable.ic_launcher_foreground)
        }

        Log.i("Image", song.image.toString())
        Log.i("Download", song.downloadUrl.toString())

        binding.root.setOnClickListener {
            val fragment =
                PlayerSheetFragment.newInstance(
                    imageToLoad?.url ?: "",
                    song.name,
                    downloadURl96kbps,
                    artists,
                )
            fragment.show((binding.root.context as AppCompatActivity).supportFragmentManager, "audioPlayer")
            Log.i("SongInfoClick", downloadURl96kbps)
        }
    }
}