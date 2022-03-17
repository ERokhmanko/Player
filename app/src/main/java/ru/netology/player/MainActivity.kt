package ru.netology.player

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import ru.netology.player.adapter.TrackAdapter
import ru.netology.player.adapter.TrackCallback
import ru.netology.player.databinding.ActivityMainBinding
import ru.netology.player.dto.Track
import ru.netology.player.viewmodel.PlayerViewModel

class MainActivity : AppCompatActivity() {
    private val mediaObserver = MediaLifecycleObserver()
    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = TrackAdapter(object : TrackCallback {
            override fun play(track: Track) {
                trackCycle(track)
            }

        })

        binding.listTrack.adapter = adapter

        setSupportActionBar(findViewById(R.id.toolbar))

        viewModel.data.observe(this) { album ->
            adapter.submitList(album.tracks
                .map {
                    it.copy(titleAlbum = album.title)
                })

            with(binding) {
                genre.text = album.genre
                nameAlbum.text = album.title
                years.text = album.published
                namePerformer.text = album.artist

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) mediaObserver.player?.seekTo(progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })

                fab.setOnClickListener {

                    if (viewModel.trackPosn.value == null) {
                        val firstTrack = viewModel.data.value?.tracks?.first()
                        if (firstTrack != null) {
                            trackCycle(firstTrack)
                        }
                    } else {
                        viewModel.data.value?.tracks?.forEach {
                            if (it.id == viewModel.trackPosn.value) {
                                val track = it
                                trackCycle(track)
                            }
                        }
                    }
                }
                next.setOnClickListener { playNextTrack() }
                prev.setOnClickListener { playPrevTrack() }
            }
        }

        lifecycle.addObserver(mediaObserver)

    }


    @SuppressLint("ObsoleteSdkInt")
    fun trackCycle(track: Track) {

        mediaObserver.player?.setOnCompletionListener {
            playNextTrack()
        }

        if (track.id != viewModel.trackPosn.value) {
            mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_STOP)
        }
        if (mediaObserver.player?.isPlaying == true) {
            mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_PAUSE)
            binding.fab.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            viewModel.imagePlay(track.id)
        } else {
            binding.fab.setImageResource(R.drawable.ic_baseline_pause_24)

            viewModel.imagePause(track.id)
            if (track.id != viewModel.trackPosn.value) {
                mediaObserver.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        player?.setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                    }
                    player?.setDataSource("${BuildConfig.BASE_URL}${track.file}")
                }.play()
                initialiseSeekBar()
            } else {
                mediaObserver.player?.start()
            }
        }
        viewModel.play(track.id)
    }

    private fun initialiseSeekBar() {

        with(binding) {
            prev.visibility = View.VISIBLE
            next.visibility = View.VISIBLE
            seekBar.visibility = View.VISIBLE

            seekBar.max = mediaObserver.player!!.duration


            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    try {
                        seekBar.progress = mediaObserver.player?.currentPosition!!
                        handler.postDelayed(this, 1000)
                    } catch (e: Exception) {
                        seekBar.progress = 0
                    }
                }
            }, 0)
        }
    }

    private fun playNextTrack() {
        viewModel.data.value?.let {
            track = if (viewModel.trackPosn.value?.toInt() == it.tracks.size) {
                it.tracks.first()
            } else {
                it.tracks[viewModel.trackPosn.value!!.toInt()]
            }
        }
        trackCycle(track)
    }

    private fun playPrevTrack() {
        viewModel.data.value?.let {
            track = if (viewModel.trackPosn.value?.toInt() == 1) {
                it.tracks.last()
            } else {
                it.tracks[viewModel.trackPosn.value!!.toInt() - 2]
            }
        }
        trackCycle(track)
    }


    override fun onStop() {
        if (mediaObserver.player?.isPlaying == true) {
            mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_PAUSE)
        }
        super.onStop()
    }

}


