package ru.netology.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
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
//        binding.toolbarLayout.title = title TODO ??


        viewModel.data.observe(this) { album ->
            adapter.submitList(album.tracks.map {
                    it.copy(titleAlbum = album.title)
                })
            binding.nameAlbum.text = album.title
        }


        lifecycle.addObserver(mediaObserver)

        binding.fab.setOnClickListener {
            val firstTrack = viewModel.data.value?.tracks?.first()
//            trackCycle(firstTrack)
        }
    }


    fun trackCycle(track: Track) {
//        mediaObserver.player?.setOnCompletionListener {
////            playNextTrack()
//        }
//
        if (track.id != viewModel.trackPosn.value) {
            mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_STOP)
        }
        if (mediaObserver.player?.isPlaying == true) {
            mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_PAUSE)
        } else {
//            if (track.id != viewModel.trackPosn.value) {
                mediaObserver.apply {
                    player?.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    player?.setDataSource("${BuildConfig.BASE_URL}${track.file}")
                }.play()


            }
//            else {
//                mediaObserver.player?.start()
//            }
//        }
        viewModel.play(track.id)
    }

//    override fun onStop() {
//        if (mediaObserver.player?.isPlaying == true) {
//            mediaObserver.onStateChanged(this@MainActivity, Lifecycle.Event.ON_PAUSE)
//        }
//        super.onStop()
//    }
}


