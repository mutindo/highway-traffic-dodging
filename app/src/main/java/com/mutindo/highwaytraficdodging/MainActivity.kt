package com.mutindo.highwaytraficdodging

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var gamePreferences: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gamePreferences = GamePreferences(this)

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        updateScores()
    }

    private fun setupUI() {
        val playButton = findViewById<Button>(R.id.playButton)
        val garageButton = findViewById<Button>(R.id.garajeButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        updateScores()

        playButton.setOnClickListener {
            startGame()
        }

        garageButton.setOnClickListener {
            openGarage()
        }

        settingsButton.setOnClickListener {
            openSettings()
        }
    }

    private fun updateScores() {
        val score = gamePreferences.getHighScore()
        val coins = gamePreferences.getCoins()
        val highScoreText = findViewById<TextView>(R.id.highScoreText)
        val coinsText = findViewById<TextView>(R.id.coinsText)

        highScoreText.text = "High Score: $score"
        coinsText.text = "Coins: $coins"
    }

    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    private fun openGarage() {
        // TODO: Implement garage activity
    }

    private fun openSettings() {
        // TODO: Implement settings activity
    }
}
