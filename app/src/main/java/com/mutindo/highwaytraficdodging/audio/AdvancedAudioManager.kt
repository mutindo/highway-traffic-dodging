package com.mutindo.highwaytraficdodging.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build

class AdvancedAudioManager(private val context: Context) {
    private val soundPool: SoundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        SoundPool.Builder()
            .setMaxStreams(16)
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            .build()
    } else {
        @Suppress("DEPRECATION")
        SoundPool(16, android.media.AudioManager.STREAM_MUSIC, 0)
    }
    
    private var backgroundMusic: MediaPlayer? = null
    private var musicLoaded = false
    private val soundEffects = mutableMapOf<String, Int>()
    private val soundStreams = mutableMapOf<String, Int>()
    private var masterVolume = 1.0f
    private var musicVolume = 0.7f
    private var sfxVolume = 0.8f
    
    init {
        loadAllSounds()
    }
    
    private fun loadAllSounds() {
        // Load multiple sound effects for variety
        val soundFiles = mapOf(
            "collision_1" to "collision_hard",
            "collision_2" to "collision_medium",
            "collision_3" to "collision_soft",
            "coin_collect_1" to "coin_ring_1",
            "coin_collect_2" to "coin_ring_2",
            "coin_collect_3" to "coin_ring_3",
            "engine_idle" to "engine_idle_smooth",
            "engine_accelerate" to "engine_accelerate",
            "engine_drift" to "engine_drift",
            "power_up" to "power_up_activate",
            "game_over" to "game_over_sound",
            "UI_click" to "menu_click",
            "car_horn" to "horn_sound",
            "tire_screech" to "tire_squeal"
        )
        
        // Note: In production, these would load from res/raw/ files
        // For now, they're placeholders that would total ~8MB when loaded
        for ((key, _) in soundFiles) {
            soundEffects[key] = soundPool.load(context, 0, 1)
        }
    }
    
    fun playSound(soundKey: String, priority: Int = 1) {
        soundEffects[soundKey]?.let { soundId ->
            val streamId = soundPool.play(
                soundId,
                sfxVolume * masterVolume,
                sfxVolume * masterVolume,
                priority,
                0,
                1f
            )
            soundStreams[soundKey] = streamId
        }
    }
    
    fun playSoundWithVariation(baseSoundKey: String) {
        val variation = (1..3).random()
        val soundKey = "${baseSoundKey}_$variation"
        playSound(soundKey)
    }
    
    fun startBackgroundMusic() {
        if (musicLoaded && backgroundMusic == null) {
            try {
                backgroundMusic = MediaPlayer().apply {
                    // Would load from res/raw/background_music.mp3
                    // For now, creating placeholder with proper setup
                    isLooping = true
                    setVolume(musicVolume * masterVolume, musicVolume * masterVolume)
                    // start() would be called after setDataSource
                }
                musicLoaded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun pauseBackgroundMusic() {
        backgroundMusic?.pause()
    }
    
    fun resumeBackgroundMusic() {
        backgroundMusic?.start()
    }
    
    fun stopBackgroundMusic() {
        backgroundMusic?.stop()
        backgroundMusic?.release()
        backgroundMusic = null
        musicLoaded = false
    }
    
    fun stopSound(soundKey: String) {
        soundStreams[soundKey]?.let { streamId ->
            soundPool.stop(streamId)
            soundStreams.remove(soundKey)
        }
    }
    
    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        backgroundMusic?.setVolume(
            musicVolume * masterVolume,
            musicVolume * masterVolume
        )
    }
    
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        backgroundMusic?.setVolume(
            musicVolume * masterVolume,
            musicVolume * masterVolume
        )
    }
    
    fun setSFXVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }
    
    fun createSoundtrack(trackName: String): SoundTrack {
        return SoundTrack(trackName, soundPool, soundEffects, context)
    }
    
    fun release() {
        soundPool.release()
        stopBackgroundMusic()
    }
}

class SoundTrack(val name: String, val soundPool: SoundPool, val effects: Map<String, Int>, val context: Context) {
    private val layers = mutableListOf<Int>()
    
    fun addLayer(soundKey: String) {
        effects[soundKey]?.let { soundId ->
            val streamId = soundPool.play(soundId, 0.5f, 0.5f, 0, -1, 1f)
            layers.add(streamId)
        }
    }
    
    fun stop() {
        layers.forEach { streamId ->
            soundPool.stop(streamId)
        }
        layers.clear()
    }
}
