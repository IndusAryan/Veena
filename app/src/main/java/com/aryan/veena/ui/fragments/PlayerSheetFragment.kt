package com.aryan.veena.ui.fragments

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import coil.load
import com.aryan.veena.R
import com.aryan.veena.databinding.PlayerBottomSheetBinding
import com.aryan.veena.helpers.DownloadHelper.downloadFile
import com.aryan.veena.services.PlayService
//import com.aryan.veena.services.PlaybackService
//import com.aryan.veena.utils.NowPlayingNotification
import com.aryan.veena.utils.NowPlayingNotificatio
import com.aryan.veena.viewmodels.PlayerSheetViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.util.concurrent.MoreExecutors
import java.util.Locale

class PlayerSheetFragment : BottomSheetDialogFragment() {

    private var _binding: PlayerBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null
    private lateinit var viewModel: PlayerSheetViewModel
    private var updatePositionRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private var thumbnailBitmap: Bitmap? = null

    private var mediaController: MediaController? = null
    var mediaSession: MediaSession? = null
    private var playbackService : PlayService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, service: IBinder?) {
            val binder = service as PlayService.PlaybackServiceBinder
            playbackService = binder.getService()
            mediaSession = binder.getMediaSession()
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            playbackService = null
            mediaSession = null
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(context, PlayService::class.java)
        context?.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        setupMediaController()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlayerBottomSheetBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[PlayerSheetViewModel::class.java]

        // Retrieve and set the song details
        val imageUrl = arguments?.getString("image_url")
        val songTitle = arguments?.getString("song_name")
        val songUrl = arguments?.getString("song_url")
        val songArtist = arguments?.getString("song_artist")

        binding.songName.text = songTitle
        binding.artistName.text = songArtist

        binding.songImage.load(imageUrl) {
            crossfade(true)
            target {
                thumbnailBitmap = it.toBitmap()
                binding.songImage.setImageDrawable(it)
            }
        }

        viewModel.play(songUrl)
        setupPlayButton()
        setupDownloadButton(songUrl, songTitle, songArtist)

        viewModel.playerLiveData.observe(viewLifecycleOwner) { player ->
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {

                    binding.exoDuration.text = formatTime(player.duration)

                    if (state == Player.STATE_READY) {
                        binding.seekBar.max = player.duration.toInt()
                        val currentPosition = player.currentPosition
                        binding.seekBar.progress = currentPosition.toInt()
                        startPositionUpdates()

                        //if (mediaSession != null) {
                            // Create and show the notification
                            val notification = NowPlayingNotificatio.createMediaNotification(
                                context ?: return,
                                songTitle ?: "",
                                thumbnailBitmap
                            )

                            NowPlayingNotificatio.showNotification(context ?: return, notification)
                        //}
                    } else if (state == Player.STATE_IDLE || state == Player.STATE_ENDED) {
                        stopPositionUpdates()
                    }
                }
            })

            setupSeekBar(player)
        }

        return binding.root
    }

    //fun observePlayerViewModel() { }

    private fun setupMediaController() {
        val sessionCTX = context ?: return
        val sessionToken = SessionToken(sessionCTX, ComponentName(sessionCTX, PlayService::class.java))
        val controllerFuture = MediaController.Builder(sessionCTX, sessionToken).buildAsync()
        controllerFuture.addListener({ /** NOT IMPLEMENTED **/ }, MoreExecutors.directExecutor())
    }

    private fun setupPlayButton() {
        binding.playButton.setOnClickListener {
            if (viewModel.isPlaying()) {
                viewModel.pause()
                binding.playButton.setIconResource(android.R.drawable.ic_media_play)
                binding.playButton.text = getString(R.string.resume)
            }
            else {
                viewModel.resume()
                binding.playButton.setIconResource(android.R.drawable.ic_media_pause)
                binding.playButton.text = getString(R.string.pause)
                startPositionUpdates()
            }
        }
    }

    private fun setupDownloadButton(songUrl: String?, songTitle: String?, songArtist: String?) {
            val downloadURL = songUrl.toString()
            val title = songTitle.toString()
            val artist = songArtist.toString()

        binding.download.setOnClickListener {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.Q)
                downloadFile(context ?: return@setOnClickListener, downloadURL, title, artist)
        }
    }

    private fun setupSeekBar(player: ExoPlayer) {
        viewModel.playerLiveData.observe(viewLifecycleOwner) {
            this.player = player
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        viewModel.seekTo(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) { /** NOT IMPLEMENTED **/ }
                override fun onStopTrackingTouch(seekBar: SeekBar?) { /** NOT IMPLEMENTED **/ }
            })
        }
    }

    override fun onStop() {
        super.onStop()
        context?.unbindService(serviceConnection)
    }

    private fun startPositionUpdates() {
        updatePositionRunnable = object : Runnable {
            override fun run() {
                if (player?.isPlaying == true) {
                    val currentPosition = player?.currentPosition ?: 0L
                    binding.seekBar.progress = currentPosition.toInt()
                    binding.exoPosition.text = formatTime(currentPosition)
                    handler.postDelayed(this, 1000) // Update every second
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        stopPositionUpdates()
        viewModel.apply {
            pause()
            saveCurrentPosition()
            releasePlayer()
        }
        mediaController?.release()
        mediaSession?.release()

        // clear now playing notification if player closed
        val nManager: NotificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nManager.cancel(NowPlayingNotificatio.NOTIFICATION_ID)
    }

    companion object {
        fun newInstance(
            imageUrl: String?,
            songName: String,
            songUrl: String,
            songArtist: String?
        ): PlayerSheetFragment {
            val fragment = PlayerSheetFragment()
            val args = Bundle()
            args.putString("image_url", imageUrl)
            args.putString("song_name", songName)
            args.putString("song_url", songUrl)
            args.putString("song_artist", songArtist)
            fragment.arguments = args
            return fragment
        }
    }
}