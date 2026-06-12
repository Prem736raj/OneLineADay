package com.onelineaday.dailydiary.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioPlayerHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    fun play(audioFile: File) {
        if (!audioFile.exists()) return
        
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, Uri.fromFile(audioFile))
                mediaPlayer?.setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                }
            }
            mediaPlayer?.start()
            _isPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }
    
    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPosition.value = 0
    }
    
    fun release() {
        stop()
    }
}
