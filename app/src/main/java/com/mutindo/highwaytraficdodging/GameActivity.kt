package com.mutindo.highwaytraficdodging

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.mutindo.highwaytraficdodging.game.GameEngine
import com.mutindo.highwaytraficdodging.game.GameView

class GameActivity : AppCompatActivity() {
    private lateinit var gameView: GameView
    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = GameView(this)
        gameEngine = GameEngine(this)
        gameView.setGameEngine(gameEngine)
        setContentView(gameView)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            gameEngine.handleTouchEvent(it)
        }
        return super.onTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()
        gameView.onResume()
    }

    override fun onPause() {
        gameView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        gameView.destroy()
        super.onDestroy()
    }
}
