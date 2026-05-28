package com.mutindo.highwaytraficdodging.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.GLU
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class GameRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val roadSegments = mutableListOf<RoadSegment>()
    private val trafficCars = mutableListOf<Car3D>()
    private val coins = mutableListOf<Coin3D>()
    private var playerCar: Car3D? = null
    private var cameraZ = 0f
    private var roadOffset = 0f
    private var score = 0
    private var gameSpeed = 0.5f

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        gl.glClearColor(0.2f, 0.6f, 1.0f, 1.0f)
        gl.glEnable(GL10.GL_DEPTH_TEST)
        gl.glEnable(GL10.GL_LIGHTING)
        gl.glEnable(GL10.GL_LIGHT0)
        gl.glEnable(GL10.GL_LIGHT1)
        gl.glEnable(GL10.GL_MATERIAL_A)
        gl.glShadeModel(GL10.GL_SMOOTH)
        
        // Initialize road segments
        for (i in 0..50) {
            roadSegments.add(RoadSegment(i * 2f))
        }
        
        // Initialize player car
        playerCar = Car3D(0f, -1f, -5f, CarType.PLAYER)
        
        // Initialize traffic cars
        for (i in 0..5) {
            trafficCars.add(Car3D((i % 4 - 1.5f), -1f, -10f - (i * 3f), CarType.TRAFFIC))
        }
        
        // Initialize coins
        for (i in 0..3) {
            coins.add(Coin3D((i % 4 - 1.5f), 0.5f, -8f - (i * 5f)))
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        gl.glViewport(0, 0, width, height)
        gl.glMatrixMode(GL10.GL_PROJECTION)
        gl.glLoadIdentity()
        GLU.gluPerspective(gl, 45.0f, width.toFloat() / height, 0.1f, 100.0f)
        gl.glMatrixMode(GL10.GL_MODELVIEW)
    }

    override fun onDrawFrame(gl: GL10) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glLoadIdentity()
        
        // Set camera
        cameraZ -= gameSpeed
        GLU.gluLookAt(
            gl,
            0f, 2f, cameraZ + 5f,  // camera position
            0f, -1f, cameraZ - 10f, // look at
            0f, 1f, 0f              // up
        )
        
        // Draw road
        drawRoad(gl)
        
        // Draw traffic cars
        trafficCars.forEach { car ->
            car.draw(gl)
            car.position.z += gameSpeed
        }
        
        // Draw coins
        coins.forEach { coin ->
            coin.draw(gl)
            coin.position.z += gameSpeed
            coin.rotation += 5f
        }
        
        // Draw player car
        playerCar?.draw(gl)
        
        // Update score
        score++
        if (score % 100 == 0) gameSpeed += 0.01f
    }
    
    private fun drawRoad(gl: GL10) {
        // Draw road as textured quad
        gl.glBegin(GL10.GL_QUADS)
        gl.glColor4f(0.3f, 0.3f, 0.3f, 1.0f)
        
        val roadWidth = 4f
        val segmentLength = 2f
        
        for (segment in roadSegments) {
            val z = segment.position - cameraZ
            if (z > -20f && z < 20f) {
                // Front quad
                gl.glVertex3f(-roadWidth, -1f, z)
                gl.glVertex3f(roadWidth, -1f, z)
                gl.glVertex3f(roadWidth, -1f, z + segmentLength)
                gl.glVertex3f(-roadWidth, -1f, z + segmentLength)
            }
        }
        gl.glEnd()
        
        // Draw lane markings
        drawLaneMarkings(gl)
    }
    
    private fun drawLaneMarkings(gl: GL10) {
        gl.glColor4f(1.0f, 1.0f, 0.0f, 0.7f)
        gl.glBegin(GL10.GL_LINES)
        
        for (i in 0..40) {
            val z = (i * 2f) - cameraZ
            if (z > -20f && z < 20f) {
                // Lane dividers
                gl.glVertex3f(-1.5f, -0.9f, z)
                gl.glVertex3f(-1.5f, -0.9f, z + 1f)
                
                gl.glVertex3f(-0.5f, -0.9f, z)
                gl.glVertex3f(-0.5f, -0.9f, z + 1f)
                
                gl.glVertex3f(0.5f, -0.9f, z)
                gl.glVertex3f(0.5f, -0.9f, z + 1f)
                
                gl.glVertex3f(1.5f, -0.9f, z)
                gl.glVertex3f(1.5f, -0.9f, z + 1f)
            }
        }
        gl.glEnd()
    }
    
    fun movePlayerCar(direction: Float) {
        playerCar?.position?.x = playerCar?.position?.x?.plus(direction) ?: 0f
        playerCar?.position?.x = playerCar?.position?.x?.coerceIn(-1.8f, 1.8f)
    }
    
    fun getScore(): Int = score
}

data class Vector3(var x: Float, var y: Float, var z: Float)

enum class CarType {
    PLAYER, TRAFFIC
}

class Car3D(x: Float, y: Float, z: Float, val type: CarType) {
    val position = Vector3(x, y, z)
    var rotation = 0f
    private val width = 0.6f
    private val height = 0.8f
    private val length = 1.2f
    
    fun draw(gl: GL10) {
        gl.glPushMatrix()
        gl.glTranslatef(position.x, position.y, position.z)
        gl.glRotatef(rotation, 0f, 1f, 0f)
        
        // Set material color based on car type
        if (type == CarType.PLAYER) {
            gl.glColor4f(1.0f, 0.4f, 0.0f, 1.0f)  // Orange
        } else {
            val colors = listOf(
                Triple(1.0f, 0.0f, 0.0f),  // Red
                Triple(0.0f, 0.0f, 1.0f),  // Blue
                Triple(0.0f, 1.0f, 0.0f),  // Green
                Triple(1.0f, 1.0f, 0.0f)   // Yellow
            )
            val color = colors[(position.x * 2).toInt() % colors.size]
            gl.glColor4f(color.first, color.second, color.third, 1.0f)
        }
        
        // Draw car body
        drawCube(gl, width, height, length)
        
        // Draw headlights
        gl.glColor4f(1.0f, 1.0f, 0.8f, 1.0f)
        gl.glPushMatrix()
        gl.glTranslatef(-width/2 + 0.1f, height/2 - 0.2f, length/2 + 0.1f)
        drawSphere(gl, 0.1f, 8, 8)
        gl.glPopMatrix()
        
        gl.glPushMatrix()
        gl.glTranslatef(width/2 - 0.1f, height/2 - 0.2f, length/2 + 0.1f)
        drawSphere(gl, 0.1f, 8, 8)
        gl.glPopMatrix()
        
        gl.glPopMatrix()
    }
    
    private fun drawCube(gl: GL10, w: Float, h: Float, l: Float) {
        val hw = w / 2
        val hh = h / 2
        val hl = l / 2
        
        gl.glBegin(GL10.GL_QUADS)
        
        // Front face
        gl.glVertex3f(-hw, -hh, hl)
        gl.glVertex3f(hw, -hh, hl)
        gl.glVertex3f(hw, hh, hl)
        gl.glVertex3f(-hw, hh, hl)
        
        // Back face
        gl.glVertex3f(-hw, -hh, -hl)
        gl.glVertex3f(-hw, hh, -hl)
        gl.glVertex3f(hw, hh, -hl)
        gl.glVertex3f(hw, -hh, -hl)
        
        // Top face
        gl.glVertex3f(-hw, hh, -hl)
        gl.glVertex3f(-hw, hh, hl)
        gl.glVertex3f(hw, hh, hl)
        gl.glVertex3f(hw, hh, -hl)
        
        // Bottom face
        gl.glVertex3f(-hw, -hh, -hl)
        gl.glVertex3f(hw, -hh, -hl)
        gl.glVertex3f(hw, -hh, hl)
        gl.glVertex3f(-hw, -hh, hl)
        
        // Right face
        gl.glVertex3f(hw, -hh, -hl)
        gl.glVertex3f(hw, hh, -hl)
        gl.glVertex3f(hw, hh, hl)
        gl.glVertex3f(hw, -hh, hl)
        
        // Left face
        gl.glVertex3f(-hw, -hh, -hl)
        gl.glVertex3f(-hw, -hh, hl)
        gl.glVertex3f(-hw, hh, hl)
        gl.glVertex3f(-hw, hh, -hl)
        
        gl.glEnd()
    }
    
    private fun drawSphere(gl: GL10, radius: Float, slices: Int, stacks: Int) {
        val quad = GLU.gluNewQuadric()
        GLU.gluSphere(quad, radius.toDouble(), slices, stacks)
    }
}

class Coin3D(x: Float, y: Float, z: Float) {
    val position = Vector3(x, y, z)
    var rotation = 0f
    
    fun draw(gl: GL10) {
        gl.glPushMatrix()
        gl.glTranslatef(position.x, position.y, position.z)
        gl.glRotatef(rotation, 0f, 1f, 0f)
        
        gl.glColor4f(1.0f, 0.84f, 0.0f, 1.0f)
        
        val quad = GLU.gluNewQuadric()
        GLU.gluSphere(quad, 0.25, 16, 16)
        
        gl.glPopMatrix()
    }
}

class RoadSegment(val position: Float)
