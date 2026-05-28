package com.mutindo.highwaytraficdodging.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool

class AudioManager(private val context: Context) {
    private var soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .build()
    
    private var backgroundMusic: MediaPlayer? = null
    private val soundEffects = mutableMapOf<String, Int>()
    
    init {
        loadSounds()
    }
    
    private fun loadSounds() {
        // Load sound effects (these would be actual audio files in res/raw/)
        // soundEffects["collision"] = soundPool.load(context, R.raw.collision, 1)
        // soundEffects["coin"] = soundPool.load(context, R.raw.coin_collect, 1)
        // soundEffects["engine"] = soundPool.load(context, R.raw.engine_sound, 1)
    }
    
    fun playSound(soundKey: String) {
        soundEffects[soundKey]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }
    
    fun startBackgroundMusic() {
        // backgroundMusic = MediaPlayer.create(context, R.raw.background_music)
        // backgroundMusic?.isLooping = true
        // backgroundMusic?.start()
    }
    
    fun stopBackgroundMusic() {
        backgroundMusic?.stop()
        backgroundMusic?.release()
        backgroundMusic = null
    }
    
    fun release() {
        soundPool.release()
        stopBackgroundMusic()
    }
}
