package com.aryan.veena.ui.fragments

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.load
import com.aryan.veena.R
import com.aryan.veena.databinding.PlayerFragmentBinding
import com.aryan.veena.helpers.DownloadHelper.downloadFile
import com.aryan.veena.repository.datamodels.NowPlayingModel
import com.aryan.veena.repository.datamodels.YTMKTVideoEndpoint
import com.aryan.veena.repository.piped.PipedAPI
import com.aryan.veena.services.PlayingService
import com.aryan.veena.utils.CoroutineUtils.ioScope
import com.aryan.veena.utils.CoroutineUtils.mainScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import com.google.common.util.concurrent.MoreExecutors
import dev.toastbits.ytmkt.formats.VideoFormatsEndpoint
import dev.toastbits.ytmkt.impl.youtubei.YoutubeiApi
import dev.toastbits.ytmkt.impl.youtubei.endpoint.YTMLoadSongEndpoint
import java.io.ByteArrayOutputStream
import java.util.Locale

class PlayerFragment : BottomSheetDialogFragment() {

    private var _binding: PlayerFragmentBinding? = null
    private val binding get() = _binding!!

    private var updatePositionRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private var thumbnailBitmap: Bitmap? = null
    private var isServiceClosed = false
    private var mediaController: MediaController? = null
    private var serviceClosedReceiver: BroadcastReceiver? = null

    private var playerData: NowPlayingModel? = null

    @RequiresApi(VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlayerFragmentBinding.inflate(inflater, container, false)

        arguments?.let {
            playerData = it.getParcelable(ARG_PLAYER_DATA)
        }
        return binding.root
    }

    @RequiresApi(VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serviceReceiver()

        // Retrieve and set the song details
        /*playerData?.let {
            // Use playerData fields directly
            val songID = it.id
            val songTitle = it.name
            val songUrl = it.url
            val songArtist = it.artistName
            val imageUrl = it.imageUrl
            // Update your UI with the retrieved data
        }*/

        /*if (playerData?.id != null && playerData?.url.isNullOrEmpty()) {
            //val ytURI = "https://youtu.be/$songID"
            Log.d("ID", playerData?.id.toString())
            ioScope { loadSongFromID(playerData?.id.toString() ?: return@ioScope) }
        }*/

        fun play(url: String?, id: String? = null) {
            if (url == null && id != null) {
                ioScope {
                    val streamableURL = loadSongFromID(id)
                    if (!streamableURL.isNullOrEmpty()) {
                        mainScope {
                            play(streamableURL)
                        }
                    }
                }
                return
            }

            url?.let {
                val mediaItem = MediaItem.Builder()
                    .setMediaId("media-${System.currentTimeMillis()}")
                    .setUri(url)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(playerData?.name)
                            .setArtist(playerData?.artistName)
                            .setArtworkData(
                                thumbnailBitmap?.toByteArray(),
                                MediaMetadata.PICTURE_TYPE_FRONT_COVER
                            )
                            .build()
                    )
                    .build()

                mediaController?.apply {
                    setMediaItem(mediaItem)
                    prepare()
                    play()
                }
            }
        }

        val sessionToken = SessionToken(
            context ?: return,
            ComponentName(requireContext(), PlayingService::class.java)
        )

        val controllerFuture = MediaController.Builder(context ?: return, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            setupMediaController()
            // Start playback after the controller is set up
            if (!playerData?.url.isNullOrEmpty() || playerData?.id != null) {
                play(playerData?.url, playerData?.id)
            }

        }, MoreExecutors.directExecutor())

        binding.songName.text = playerData?.name
        binding.artistName.text = playerData?.artistName

        binding.songImage.load(playerData?.imageUrl) {
            crossfade(true)
            target {
                thumbnailBitmap = it.toBitmap()
                binding.songImage.setImageDrawable(it)
            }
        }

        setupPlayButton()
        setupSeekBar()
        //setupDownloadButton(songUrl, songTitle, songArtist)
    }

    private suspend fun loadSongFromID(ytURI: String) : String? {
        var streamableYTurl : String? = null

            try {
                val videoEndpoint : VideoFormatsEndpoint = YoutubeiApi().VideoFormats
                val video = videoEndpoint.getVideoFormats(ytURI).getOrThrow()
                val url48Kbps = video.find { it.itag == 139 }?.url // mp4 mp4a
                val url53kbps = video.find { it.itag == 249 }?.url // webm opus
                val url69kbps = video.find { it.itag == 250 }?.url // webm opus
                val url126kbps = video.find { it.itag == 140 }?.url // mp4 m4a
                val url133kbps = video.find { it.itag == 251 }?.url // webm opus

                println("48KBPS $url48Kbps")
                val videoInfo = video.forEach { it ->
                        println(it.url)
                        println(it.itag)
                        println(it.audioTrack)
                        println(it.audioTrackType)
                        println(it.bitrate)
                        println(it.mimeType)

                }
                streamableYTurl = url48Kbps
                /*val fetchURI = "https://www.youtube.com/watch?v=$ytURI"
                println(fetchURI)
                val pipedStream = PipedAPI.pipedService.getStreamURL(fetchURI)
                println(pipedStream.root.audioStreams)*/
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        println(streamableYTurl)
        return streamableYTurl
    }

    @RequiresApi(VERSION_CODES.O)
    private fun serviceReceiver() {
        // Initialize and register the BroadcastReceiver
        serviceClosedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.aryan.veena.PLAYING_SERVICE_CLOSED") {
                    Log.d("YourFragment", "Received broadcast: PLAYING_SERVICE_CLOSED")
                    isServiceClosed = true
                    if (!isStateSaved) {
                        dismiss()
                    }
                }
            }
        }
        context?.registerReceiver(serviceClosedReceiver, IntentFilter("com.aryan.veena.PLAYING_SERVICE_CLOSED"),
            Context.RECEIVER_NOT_EXPORTED)
    }

    private fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 1, stream)
        return stream.toByteArray()
    }

    private fun setupSeekBar() {
        binding.seekBar.apply {
            setCustomThumbDrawable(R.drawable.custom_thumb)
            addOnChangeListener(Slider.OnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    mediaController?.seekTo(value.toLong())
                }
            })
            setLabelFormatter {
                formatTime(mediaController?.currentPosition ?: 0)
            }
        }
    }

    private fun setupMediaController() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    mediaController?.duration?.let { duration ->
                        binding.seekBar.valueTo = duration.toFloat()
                        binding.seekBar.valueFrom = 0.0f
                    }

                    mediaController?.currentPosition?.let { currentPosition ->
                        binding.seekBar.value = currentPosition.toFloat()
                    }
                    startPositionUpdates()
                } else if (state == Player.STATE_IDLE || state == Player.STATE_ENDED) {
                    stopPositionUpdates()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("PlayerFragment", "Player error: ${error.message}")
            }
        })
    }

    private fun setupPlayButton() {
        binding.playButton.setOnClickListener {
            mediaController?.let {
                if (it.isPlaying) {
                    it.pause()
                    binding.playButton.setIconResource(android.R.drawable.ic_media_play)
                    binding.playButton.text = getString(R.string.resume)
                } else {
                    it.play()
                    binding.playButton.setIconResource(android.R.drawable.ic_media_pause)
                    binding.playButton.text = getString(R.string.pause)
                    startPositionUpdates()
                }
            }
        }
    }

    private fun setupDownloadButton(songUrl: String, songTitle: String, songArtist: String) {
        binding.download.setOnClickListener {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.Q)
                downloadFile(context ?: return@setOnClickListener, songUrl, songTitle, songArtist)
        }
    }

    private fun startPositionUpdates() {
        updatePositionRunnable = object : Runnable {
            override fun run() {
                mediaController?.let {
                    if (it.isPlaying) {
                        val currentPosition = mediaController?.currentPosition ?: return@let
                        val duration = it.duration
                        if (currentPosition in 0..duration) {
                            binding.seekBar.value = currentPosition.toFloat()
                            binding.exoPosition.text = formatTime(currentPosition)
                            binding.exoDuration.text = formatTime(duration)
                        }
                        handler.postDelayed(this, 1000) // Update every second
                    }
                }
            }
        }
        handler.post(updatePositionRunnable ?: return)
    }

    private fun stopPositionUpdates() {
        updatePositionRunnable?.let {
            handler.removeCallbacks(it)
        }
        updatePositionRunnable = null
    }

    // format milliseconds into readable time i.e, 12:00 for player
    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        //Log.d("time", "$minutes $seconds")
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unregisterReceiver(serviceClosedReceiver)
    }

    override fun onResume() {
        super.onResume()
        // Check the flag and dismiss the fragment if the service was closed
        if (isServiceClosed) {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopPositionUpdates()
        mediaController?.release()
    }

    companion object {
        private const val ARG_PLAYER_DATA = "player_data"
        fun newInstance(
            playerData: NowPlayingModel
        ): PlayerFragment {
            return PlayerFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PLAYER_DATA, playerData)
                }
            }
        }
    }
}