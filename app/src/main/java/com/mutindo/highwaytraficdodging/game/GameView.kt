package com.mutindo.highwaytraficdodging.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs) {
    private lateinit var gameEngine: GameEngine
    private val gameThread = GameThread()
    private var surfaceCreated = false

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                surfaceCreated = true
                if (::gameEngine.isInitialized) {
                    gameEngine.initialize(width, height)
                    gameThread.setRunning(true)
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                if (::gameEngine.isInitialized) {
                    gameEngine.initialize(width, height)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
    }

    fun setGameEngine(engine: GameEngine) {
        gameEngine = engine
        gameEngine.initialize(width, height)
    }

    fun onResume() {
        gameThread.setRunning(true)
    }

    fun onPause() {
        gameThread.setRunning(false)
    }

    fun destroy() {
        gameThread.setRunning(false)
    }

    private inner class GameThread : Thread() {
        private var running = false
        private var lastTime = System.currentTimeMillis()
        private val paint = Paint().apply {
            isAntiAlias = true
            textSize = 48f
        }

        fun setRunning(running: Boolean) {
            this.running = running
            if (running && !isAlive) {
                start()
            }
        }

        override fun run() {
            while (running && ::gameEngine.isInitialized) {
                try {
                    val currentTime = System.currentTimeMillis()
                    val deltaTime = ((currentTime - lastTime) / 1000f).coerceAtMost(0.05f)
                    lastTime = currentTime

                    gameEngine.update(deltaTime)

                    val holder = holder
                    if (holder.surface.isValid) {
                        val canvas = holder.lockCanvas()
                        if (canvas != null) {
                            drawGame(canvas, paint)
                            holder.unlockCanvasAndPost(canvas)
                        }
                    }
                    Thread.sleep(16)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun drawGame(canvas: Canvas, paint: Paint) {
            canvas.drawColor(0xFF87CEEB.toInt())
            drawRoad(canvas)
            drawPlayer(canvas, paint)
            drawTraffic(canvas, paint)
            drawCoins(canvas, paint)
            drawUI(canvas, paint)
        }

        private fun drawRoad(canvas: Canvas) {
            canvas.drawRect(100f, 0f, canvas.width - 100f, canvas.height.toFloat(), Paint().apply {
                color = 0xFF333333.toInt()
            })
            drawLaneMarkings(canvas)
        }

        private fun drawLaneMarkings(canvas: Canvas) {
            val paint = Paint().apply {
                color = 0xFFFFFF00.toInt()
                strokeWidth = 3f
            }
            for (i in 0..3) {
                val x = 120f + (i * 260f) + 40f
                canvas.drawLine(x, 0f, x, canvas.height.toFloat(), paint)
            }
        }

        private fun drawPlayer(canvas: Canvas, paint: Paint) {
            val player = gameEngine.getPlayer()
            paint.color = 0xFFFF6B00.toInt()
            canvas.drawRect(
                Rect(
                    player.x.toInt(),
                    player.y.toInt(),
                    (player.x + player.width).toInt(),
                    (player.y + player.height).toInt()
                ),
                paint
            )
            // Draw car details
            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawRect(
                Rect(
                    (player.x + 10).toInt(),
                    (player.y + 10).toInt(),
                    (player.x + player.width - 10).toInt(),
                    (player.y + 40).toInt()
                ),
                paint
            )
        }

        private fun drawTraffic(canvas: Canvas, paint: Paint) {
            gameEngine.getTrafficCars().forEach { car ->
                paint.color = car.carColor
                canvas.drawRect(
                    Rect(
                        car.x.toInt(),
                        car.y.toInt(),
                        (car.x + car.width).toInt(),
                        (car.y + car.height).toInt()
                    ),
                    paint
                )
                // Draw headlights
                paint.color = 0xFFFFFF00.toInt()
                canvas.drawRect(
                    Rect(
                        (car.x + 15).toInt(),
                        (car.y + 5).toInt(),
                        (car.x + 30).toInt(),
                        (car.y + 15).toInt()
                    ),
                    paint
                )
                canvas.drawRect(
                    Rect(
                        (car.x + 50).toInt(),
                        (car.y + 5).toInt(),
                        (car.x + 65).toInt(),
                        (car.y + 15).toInt()
                    ),
                    paint
                )
            }
        }

        private fun drawCoins(canvas: Canvas, paint: Paint) {
            gameEngine.getCoinsList().forEach { coin ->
                paint.color = 0xFFFFD700.toInt()
                canvas.drawCircle(
                    coin.x + coin.width / 2,
                    coin.y + coin.height / 2,
                    coin.width / 2,
                    paint
                )
                paint.color = 0xFFCC9900.toInt()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawCircle(
                    coin.x + coin.width / 2,
                    coin.y + coin.height / 2,
                    coin.width / 2 - 2,
                    paint
                )
                paint.style = Paint.Style.FILL
            }
        }

        private fun drawUI(canvas: Canvas, paint: Paint) {
            paint.color = 0xFF000000.toInt()
            paint.textSize = 48f
            paint.style = Paint.Style.FILL
            canvas.drawText("Score: ${gameEngine.getScore()}", 50f, 100f, paint)
            canvas.drawText("Coins: ${gameEngine.getCoins()}", 50f, 200f, paint)
            
            if (!gameEngine.isGameRunning()) {
                paint.color = 0xFF000000.toInt()
                paint.textSize = 80f
                canvas.drawText("GAME OVER", 150f, canvas.height / 2.toFloat(), paint)
                paint.textSize = 48f
                canvas.drawText("Score: ${gameEngine.getScore()}", 200f, (canvas.height / 2 + 100).toFloat(), paint)
            }
        }
    }
}
