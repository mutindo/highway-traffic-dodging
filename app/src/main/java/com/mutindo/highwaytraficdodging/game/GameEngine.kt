package com.mutindo.highwaytraficdodging.game

import android.content.Context
import android.view.MotionEvent
import com.mutindo.highwaytraficdodging.GamePreferences
import com.mutindo.highwaytraficdodging.game.objects.*
import kotlin.random.Random

class GameEngine(private val context: Context) {
    private val gamePreferences = GamePreferences(context)
    private lateinit var player: PlayerCar
    private val trafficCars = mutableListOf<TrafficCar>()
    private val coins = mutableListOf<Coin>()
    private var gameRunning = false
    private var gameScore = 0
    private var gameCoins = 0
    private var screenWidth = 1080f
    private var screenHeight = 1920f
    private var gameSpeed = 5f
    private var spawnTimer = 0f
    private var spawnInterval = 120f
    private var lastTouchX = 0f

    fun initialize(width: Int, height: Int) {
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        player = PlayerCar(screenWidth / 2 - 40, screenHeight - 200, 80, 120, gamePreferences.getSelectedCar())
        gameRunning = true
        gameScore = 0
        gameCoins = 0
        trafficCars.clear()
        coins.clear()
    }

    fun update(deltaTime: Float) {
        if (!gameRunning) return

        player.update(deltaTime)
        updateTraffic(deltaTime)
        updateCoins(deltaTime)
        checkCollisions()
        increaseScore()
    }

    private fun updateTraffic(deltaTime: Float) {
        spawnTimer += deltaTime
        if (spawnTimer > spawnInterval) {
            spawnTrafficCar()
            spawnTimer = 0f
            if (spawnInterval > 80f) spawnInterval -= 2f
        }

        trafficCars.removeAll { car ->
            car.y > screenHeight
        }

        trafficCars.forEach { car ->
            car.update(deltaTime, gameSpeed)
        }
    }

    private fun spawnTrafficCar() {
        val lane = Random.nextInt(0, 4)
        val x = 120f + (lane * 260f)
        val car = TrafficCar(x, -120f, 80, 120, lane)
        trafficCars.add(car)
    }

    private fun updateCoins(deltaTime: Float) {
        coins.removeAll { coin ->
            coin.y > screenHeight
        }

        coins.forEach { coin ->
            coin.update(deltaTime, gameSpeed)
        }
    }

    private fun checkCollisions() {
        trafficCars.forEach { car ->
            if (player.collidesWith(car)) {
                endGame()
            }
        }

        coins.removeAll { coin ->
            if (player.collidesWith(coin)) {
                gameCoins++
                gamePreferences.addCoins(1)
                true
            } else {
                false
            }
        }
    }

    private fun increaseScore() {
        gameScore += 1
        if (gameScore % 500 == 0) {
            gameSpeed += 0.5f
            if (Random.nextInt(0, 100) < 30) {
                val coin = Coin(
                    (120 + Random.nextInt(0, 4) * 260).toFloat(),
                    -50f,
                    50,
                    50
                )
                coins.add(coin)
            }
        }
    }

    fun handleTouchEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - lastTouchX
                player.move(deltaX, screenWidth)
                lastTouchX = event.x
            }
        }
    }

    private fun endGame() {
        gameRunning = false
        gamePreferences.setHighScore(gameScore)
    }

    fun isGameRunning(): Boolean = gameRunning
    fun getScore(): Int = gameScore
    fun getCoins(): Int = gameCoins
    fun getPlayer(): PlayerCar = player
    fun getTrafficCars(): List<TrafficCar> = trafficCars
    fun getCoinsList(): List<Coin> = coins
}
