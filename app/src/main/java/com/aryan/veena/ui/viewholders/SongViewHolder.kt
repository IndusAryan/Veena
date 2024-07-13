package com.aryan.veena.ui.viewholders

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.aryan.veena.R
import com.aryan.veena.api.RetrofitInstance.USER_AGENT
import com.aryan.veena.databinding.SongItemBinding
import com.aryan.veena.repository.NowPlayingModel
import com.aryan.veena.ui.fragments.PlayerFragment
import com.aryan.veena.utils.ImageLoader
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SongViewHolder(private val binding: SongItemBinding? = null) :
    RecyclerView.ViewHolder(binding?.root!!) {

    fun bind(song: NowPlayingModel) {

        val context = itemView.context
        val imageLoader = ImageLoader.getInstance(context)

        binding?.songName?.text = song.name
        binding?.songDuration?.apply {
            text = song.duration?.toLong()?.toDuration(DurationUnit.SECONDS).toString()
            if (song.duration == null) { isGone = true }
        }
        binding?.artistName?.apply {
            text = song.artistName
            if (song.artistName == null) { isGone = true }
        }

        binding?.songImage?.load(song.imageUrl, imageLoader) {
            //crossfade(true)
            placeholder(R.drawable.veena_placeholder_icons8)
            addHeader("User-Agent", USER_AGENT)
            error(R.drawable.ic_launcher_foreground)
            listener( onStart = { Log.d("ImageLoad", "Loading started") },
                onSuccess = { _, _ -> Log.d("ImageLoad", "Loading successful") },
                onError = { _, throwable -> Log.e("ImageLoadError", "Error loading image: $throwable") }
            )
        }
        Log.i("Image", song.imageUrl.toString())
        //Log.i("Download", "${song.url}")

        binding?.root?.setOnClickListener {
            val fragment =
                PlayerFragment.newInstance(
                    NowPlayingModel(
                        id = song.id,
                        url = song.url,
                        name = song.name.toString(),
                        artistName = song.artistName,
                        imageUrl = song.imageUrl ?: "",
                        provider = song.provider,
                    )
                )
            fragment.show((binding.root.context as AppCompatActivity).supportFragmentManager, "audioPlayer")
            //Log.i("SongInfoClick", "${song.url}")
        }
    }
}