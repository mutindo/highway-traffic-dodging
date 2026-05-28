package com.mutindo.highwaytraficdodging.game.objects

class PlayerCar(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    val carType: String
) {
    private var velocityX = 0f
    private val maxSpeed = 15f
    private val friction = 0.92f

    fun update(deltaTime: Float) {
        velocityX *= friction
        x += velocityX
    }

    fun move(deltaX: Float, screenWidth: Float) {
        velocityX = deltaX * 0.3f
        velocityX = velocityX.coerceIn(-maxSpeed, maxSpeed)
        x += velocityX
        x = x.coerceIn(60f, screenWidth - 140f)
    }

    fun collidesWith(other: GameObject): Boolean {
        return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y
    }
}

interface GameObject {
    val x: Float
    val y: Float
    val width: Float
    val height: Float
}
