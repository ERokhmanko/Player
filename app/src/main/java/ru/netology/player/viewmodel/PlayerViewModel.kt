package ru.netology.player.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.player.api.Api
import ru.netology.player.dto.Album
import ru.netology.player.repository.PlayerRepository
import ru.netology.player.repository.PlayerRepositoryImpl


class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlayerRepository =
        PlayerRepositoryImpl()

    private val _data = MutableLiveData<Album>()
    val data: LiveData<Album>
        get() = _data

    val trackPosn = MutableLiveData<Long>()

    init {
      loadTracks()
    } //TODO ??

    private fun loadTracks() = viewModelScope.launch {
        val album = repository.getAll()
        _data.value = album

    }

//    private fun loadTracks() = viewModelScope.launch {
//        try {
//            val response = Api.retrofitService.getAll()
//            if (response.isSuccessful) {
//                _data.value = response.body()
//            }
//        } catch (e: Exception) {
//        }
//    }

    fun play(id: Long) {
        trackPosn.value = id

//        _data.value = data.value?.let { album ->
//            album.copy(tracks = album.tracks.map { track ->
//                if (id == track.id) {
//                    track.copy(running = true)
//                } else {
//                    track.copy(running = false)
//                }
//            })
//        }
    }

}

