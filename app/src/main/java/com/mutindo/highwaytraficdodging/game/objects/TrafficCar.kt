package com.mutindo.highwaytraficdodging.game.objects

import kotlin.random.Random

class TrafficCar(
    override var x: Float,
    override var y: Float,
    override val width: Float,
    override val height: Float,
    val lane: Int
) : GameObject {
    val carColor = when (Random.nextInt(0, 4)) {
        0 -> 0xFFFF0000.toInt()
        1 -> 0xFF0000FF.toInt()
        2 -> 0xFF00FF00.toInt()
        else -> 0xFFFFFF00.toInt()
    }

    fun update(deltaTime: Float, gameSpeed: Float) {
        y += gameSpeed * 2
    }
}
