package com.mutindo.highwaytraficdodging

import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.mutindo.highwaytraficdodging.audio.AdvancedAudioManager
import com.mutindo.highwaytraficdodging.opengl.AdvancedGameRenderer

class Game3DAdvancedActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var gameRenderer: AdvancedGameRenderer
    private lateinit var audioManager: AdvancedAudioManager
    private var lastX = 0f
    private var gameActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        
        gameRenderer = AdvancedGameRenderer(this)
        glSurfaceView.setRenderer(gameRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        
        audioManager = AdvancedAudioManager(this)
        audioManager.startBackgroundMusic()
        audioManager.playSound("UI_click")
        
        setContentView(glSurfaceView)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = it.x
                    if (gameActive) {
                        audioManager.playSound("engine_accelerate")
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (gameActive) {
                        val deltaX = it.x - lastX
                        gameRenderer.movePlayerCar(deltaX * 0.015f)
                        lastX = it.x
                        
                        // Play engine sound occasionally
                        if ((Math.random() * 100).toInt() % 10 == 0) {
                            audioManager.playSoundWithVariation("engine_idle")
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    audioManager.stopSound("engine_accelerate")
                }
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
        audioManager.resumeBackgroundMusic()
        gameActive = true
    }

    override fun onPause() {
        gameActive = false
        glSurfaceView.onPause()
        audioManager.pauseBackgroundMusic()
        super.onPause()
    }

    override fun onDestroy() {
        audioManager.stopBackgroundMusic()
        audioManager.release()
        super.onDestroy()
    }
}
