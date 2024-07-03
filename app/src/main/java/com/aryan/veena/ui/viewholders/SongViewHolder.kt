package com.aryan.veena.ui.viewholders

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.aryan.veena.R
import com.aryan.veena.VeenaApp
import com.aryan.veena.databinding.SongItemBinding
import com.aryan.veena.repository.datamodels.NowPlayingModel
import com.aryan.veena.ui.fragments.PlayerFragment
import com.aryan.veena.utils.ImageLoader
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SongViewHolder(private val binding: SongItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(song: NowPlayingModel) {

        val context = itemView.context
        val imageLoader = ImageLoader.getInstance(context)

        binding.songName.text = song.name
        binding.artistName.text = song.artistName
        binding.songDuration.text = song.duration?.toDuration(DurationUnit.SECONDS).toString()

        binding.songImage.load(song.imageUrl, imageLoader) {
            crossfade(true)
            placeholder(R.drawable.ic_launcher_foreground)
            error(R.drawable.ic_launcher_foreground)
        }

        Log.i("Image", song.imageUrl.toString())
        Log.i("Download", "${song.url}")

        binding.root.setOnClickListener {
            val fragment =
                PlayerFragment.newInstance(
                    NowPlayingModel(
                        id = song.id,
                        url = song.url,
                        name = song.name.toString(),
                        artistName = song.artistName,
                        imageUrl = song.imageUrl ?: "",
                    )
                )
            fragment.show((binding.root.context as AppCompatActivity).supportFragmentManager, "audioPlayer")
            Log.i("SongInfoClick", "${song.url}")
        }
    }
}