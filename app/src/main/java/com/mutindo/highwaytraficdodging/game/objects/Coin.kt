package com.mutindo.highwaytraficdodging.game.objects

class Coin(
    override var x: Float,
    override var y: Float,
    override val width: Float,
    override val height: Float
) : GameObject {
    var rotation = 0f

    fun update(deltaTime: Float, gameSpeed: Float) {
        y += gameSpeed * 2
        rotation += 5f
        if (rotation >= 360f) rotation = 0f
    }
}
