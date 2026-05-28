package com.mutindo.highwaytraficdodging

import android.os.Bundle
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import android.opengl.GLSurfaceView
import com.mutindo.highwaytraficdodging.audio.AudioManager
import com.mutindo.highwaytraficdodging.opengl.GameRenderer

class Game3DActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var gameRenderer: GameRenderer
    private lateinit var audioManager: AudioManager
    private var lastX = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(2)
        
        gameRenderer = GameRenderer(this)
        glSurfaceView.setRenderer(gameRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        
        audioManager = AudioManager(this)
        audioManager.startBackgroundMusic()
        
        setContentView(glSurfaceView)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = it.x
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = it.x - lastX
                    gameRenderer.movePlayerCar(deltaX * 0.01f)
                    lastX = it.x
                    audioManager.playSound("engine")
                }
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
        audioManager.startBackgroundMusic()
    }

    override fun onPause() {
        glSurfaceView.onPause()
        audioManager.stopBackgroundMusic()
        super.onPause()
    }

    override fun onDestroy() {
        audioManager.release()
        super.onDestroy()
    }
}
