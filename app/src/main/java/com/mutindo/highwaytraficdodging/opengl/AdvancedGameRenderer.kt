package com.mutindo.highwaytraficdodging.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.GLU
import android.opengl.GLES20
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class AdvancedGameRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var shaderProgram: Int = 0
    private val roadSegments = mutableListOf<RoadSegment3D>()
    private val trafficCars = mutableListOf<DetailedCar3D>()
    private val coins = mutableListOf<CoinParticle>()
    private val particles = mutableListOf<Particle>()
    private var playerCar: DetailedCar3D? = null
    private var cameraZ = 0f
    private var roadOffset = 0f
    private var score = 0
    private var gameSpeed = 0.5f
    private var lightPosition = floatArrayOf(2f, 5f, 2f, 0f)
    private var ambientLight = floatArrayOf(0.3f, 0.3f, 0.3f, 1.0f)
    private var diffuseLight = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    private var specularLight = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.2f, 0.6f, 1.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        
        // Create advanced shader program
        shaderProgram = createShaderProgram()
        
        // Initialize high-detail road segments
        for (i in 0..100) {
            roadSegments.add(RoadSegment3D(i * 3f, 16))
        }
        
        // Initialize detailed player car
        playerCar = DetailedCar3D(0f, -0.8f, -5f, CarModel.PLAYER_RED, 64)
        
        // Initialize traffic cars with variations
        for (i in 0..8) {
            val carType = when (i % 5) {
                0 -> CarModel.TRAFFIC_BLUE
                1 -> CarModel.TRAFFIC_RED
                2 -> CarModel.TRAFFIC_GREEN
                3 -> CarModel.TRAFFIC_YELLOW
                else -> CarModel.TRAFFIC_PURPLE
            }
            trafficCars.add(DetailedCar3D(
                ((i % 4) - 1.5f) * 1.2f,
                -0.8f,
                -10f - (i * 4f),
                carType,
                64
            ))
        }
        
        // Initialize coins with particle effects
        for (i in 0..5) {
            coins.add(CoinParticle(
                ((i % 4) - 1.5f) * 1.2f,
                0.8f,
                -8f - (i * 6f),
                true
            ))
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        val projectionMatrix = FloatArray(16)
        val fov = 45.0f * PI.toFloat() / 180.0f
        val f = 1.0f / kotlin.math.tan(fov / 2.0f)
        projectionMatrix[0] = f / ratio
        projectionMatrix[5] = f
        projectionMatrix[10] = (100.0f + 0.1f) / (0.1f - 100.0f)
        projectionMatrix[11] = -1.0f
        projectionMatrix[14] = (2.0f * 100.0f * 0.1f) / (0.1f - 100.0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(shaderProgram)
        
        // Update camera
        cameraZ -= gameSpeed
        
        // Draw detailed road
        drawDetailedRoad()
        
        // Update and draw traffic cars
        trafficCars.forEach { car ->
            car.position.z += gameSpeed
            car.rotation.y += 1f
            car.draw(shaderProgram)
            
            // Collision check
            if (checkCollision(playerCar!!, car)) {
                createExplosionParticles(car.position.x, car.position.y, car.position.z)
            }
        }
        
        // Update and draw coins
        coins.forEach { coin ->
            coin.position.z += gameSpeed
            coin.rotation.y += 8f
            coin.draw(shaderProgram)
            
            // Coin collection
            if (checkCollision(playerCar!!, coin)) {
                createCoinCollectParticles(coin.position.x, coin.position.y, coin.position.z)
                coins.remove(coin)
            }
        }
        
        // Draw particles
        particles.forEach { particle ->
            particle.update(gameSpeed)
            particle.draw(shaderProgram)
        }
        particles.removeAll { it.isDead() }
        
        // Draw player car
        playerCar?.draw(shaderProgram)
        
        // Update game state
        score++
        if (score % 150 == 0) gameSpeed += 0.02f
        if (score % 200 == 0) createNewTrafficCar()
    }
    
    private fun drawDetailedRoad() {
        val roadWidth = 4.2f
        val segmentLength = 3f
        
        // Draw road surface with gradient
        for (segment in roadSegments) {
            val z = segment.position - cameraZ
            if (z > -25f && z < 25f) {
                drawRoadSegmentWithDetails(segment, roadWidth, segmentLength)
            }
        }
        
        // Draw lane markings with animation
        drawAnimatedLaneMarkings()
        
        // Draw road borders
        drawRoadBorders()
        
        // Draw shadows
        drawRoadShadows()
    }
    
    private fun drawRoadSegmentWithDetails(segment: RoadSegment3D, width: Float, length: Float) {
        val vertexCount = segment.vertexCount
        
        for (i in 0 until vertexCount - 1) {
            val angle = (i.toFloat() / vertexCount) * 2 * PI.toFloat()
            val nextAngle = ((i + 1).toFloat() / vertexCount) * 2 * PI.toFloat()
            
            val x1 = (width / 2) * cos(angle.toDouble()).toFloat()
            val x2 = (width / 2) * cos(nextAngle.toDouble()).toFloat()
            
            // Draw segment with texture coordinates
            GLES20.glBegin(GLES20.GL_TRIANGLE_STRIP)
            GLES20.glColor4f(0.2f, 0.2f, 0.2f, 1.0f)
            // Gradient from gray to dark
            val intensity = 0.3f + (i.toFloat() / vertexCount) * 0.5f
            GLES20.glColor4f(intensity, intensity, intensity, 1.0f)
            GLES20.glEnd()
        }
    }
    
    private fun drawAnimatedLaneMarkings() {
        val markingColor = floatArrayOf(1.0f, 1.0f, 0.0f, 0.8f)
        val markingOffset = (gameSpeed * 100) % 6f
        
        for (i in 0..50) {
            val z = (i * 3f) - cameraZ - markingOffset
            if (z > -25f && z < 25f) {
                // Lane dividers with dashed pattern
                drawDashedLine(-1.4f, -0.95f, z, -1.4f, -0.95f, z + 1.5f, markingColor)
                drawDashedLine(-0.47f, -0.95f, z, -0.47f, -0.95f, z + 1.5f, markingColor)
                drawDashedLine(0.47f, -0.95f, z, 0.47f, -0.95f, z + 1.5f, markingColor)
                drawDashedLine(1.4f, -0.95f, z, 1.4f, -0.95f, z + 1.5f, markingColor)
            }
        }
    }
    
    private fun drawDashedLine(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: FloatArray) {
        GLES20.glColor4f(color[0], color[1], color[2], color[3])
        GLES20.glBegin(GLES20.GL_LINES)
        GLES20.glVertex3f(x1, y1, z1)
        GLES20.glVertex3f(x2, y2, z2)
        GLES20.glEnd()
    }
    
    private fun drawRoadBorders() {
        val borderColor = floatArrayOf(0.8f, 0.0f, 0.0f, 1.0f)
        for (i in 0..50) {
            val z = (i * 3f) - cameraZ
            if (z > -25f && z < 25f) {
                drawBorderLine(-2.3f, -1f, z, -2.3f, -1f, z + 3f, borderColor)
                drawBorderLine(2.3f, -1f, z, 2.3f, -1f, z + 3f, borderColor)
            }
        }
    }
    
    private fun drawBorderLine(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, color: FloatArray) {
        GLES20.glColor4f(color[0], color[1], color[2], color[3])
        GLES20.glBegin(GLES20.GL_LINES)
        for (i in 0..10) {
            val t = i.toFloat() / 10f
            GLES20.glVertex3f(
                x1 + (x2 - x1) * t,
                y1 + (y2 - y1) * t,
                z1 + (z2 - z1) * t
            )
        }
        GLES20.glEnd()
    }
    
    private fun drawRoadShadows() {
        // Draw shadows of cars on the road
        trafficCars.forEach { car ->
            GLES20.glColor4f(0.0f, 0.0f, 0.0f, 0.3f)
            GLES20.glBegin(GLES20.GL_QUADS)
            val shadowWidth = car.width * 0.9f
            val shadowHeight = car.length * 0.5f
            GLES20.glVertex3f(car.position.x - shadowWidth / 2, -0.99f, car.position.z - shadowHeight / 2)
            GLES20.glVertex3f(car.position.x + shadowWidth / 2, -0.99f, car.position.z - shadowHeight / 2)
            GLES20.glVertex3f(car.position.x + shadowWidth / 2, -0.99f, car.position.z + shadowHeight / 2)
            GLES20.glVertex3f(car.position.x - shadowWidth / 2, -0.99f, car.position.z + shadowHeight / 2)
            GLES20.glEnd()
        }
    }
    
    private fun createExplosionParticles(x: Float, y: Float, z: Float) {
        for (i in 0..30) {
            val angle = (i.toFloat() / 30) * 2 * PI.toFloat()
            val velocityX = cos(angle.toDouble()).toFloat() * 0.5f
            val velocityZ = sin(angle.toDouble()).toFloat() * 0.5f
            val velocityY = 0.5f + (Math.random().toFloat() * 0.3f)
            
            particles.add(Particle(
                x, y, z,
                velocityX, velocityY, velocityZ,
                1.0f, 0.4f, 0.0f,
                lifespan = 60
            ))
        }
    }
    
    private fun createCoinCollectParticles(x: Float, y: Float, z: Float) {
        for (i in 0..20) {
            val angle = (i.toFloat() / 20) * 2 * PI.toFloat()
            val distance = 0.2f + (Math.random().toFloat() * 0.3f)
            val velocityX = cos(angle.toDouble()).toFloat() * distance
            val velocityZ = sin(angle.toDouble()).toFloat() * distance
            val velocityY = 0.3f + (Math.random().toFloat() * 0.2f)
            
            particles.add(Particle(
                x, y, z,
                velocityX, velocityY, velocityZ,
                1.0f, 0.84f, 0.0f,
                lifespan = 40
            ))
        }
    }
    
    private fun checkCollision(car1: DetailedCar3D, car2: DetailedCar3D): Boolean {
        val dx = car1.position.x - car2.position.x
        val dy = car1.position.y - car2.position.y
        val dz = car1.position.z - car2.position.z
        val distance = kotlin.math.sqrt((dx*dx + dy*dy + dz*dz).toDouble()).toFloat()
        return distance < (car1.width + car2.width)
    }
    
    private fun checkCollision(car: DetailedCar3D, coin: CoinParticle): Boolean {
        val dx = car.position.x - coin.position.x
        val dy = car.position.y - coin.position.y
        val dz = car.position.z - coin.position.z
        val distance = kotlin.math.sqrt((dx*dx + dy*dy + dz*dz).toDouble()).toFloat()
        return distance < (car.width + 0.3f)
    }
    
    private fun createNewTrafficCar() {
        val lane = (Math.random() * 4).toInt()
        val carType = when ((Math.random() * 5).toInt()) {
            0 -> CarModel.TRAFFIC_BLUE
            1 -> CarModel.TRAFFIC_RED
            2 -> CarModel.TRAFFIC_GREEN
            3 -> CarModel.TRAFFIC_YELLOW
            else -> CarModel.TRAFFIC_PURPLE
        }
        trafficCars.add(DetailedCar3D(
            ((lane - 1.5f) * 1.2f),
            -0.8f,
            cameraZ - 15f,
            carType,
            64
        ))
    }
    
    fun movePlayerCar(direction: Float) {
        playerCar?.position?.x = (playerCar?.position?.x ?: 0f) + (direction * 0.02f)
        playerCar?.position?.x = (playerCar?.position?.x ?: 0f).coerceIn(-1.8f, 1.8f)
    }
    
    fun getScore(): Int = score
    
    private fun createShaderProgram(): Int {
        val vertexShaderCode = """#version 100
            precision mediump float;
            attribute vec3 aPosition;
            attribute vec3 aNormal;
            attribute vec2 aTexCoord;
            varying vec3 vNormal;
            varying vec2 vTexCoord;
            uniform mat4 uMVPMatrix;
            uniform mat4 uModelMatrix;
            void main() {
                gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
                vNormal = normalize((uModelMatrix * vec4(aNormal, 0.0)).xyz);
                vTexCoord = aTexCoord;
            }
        """
        
        val fragmentShaderCode = """#version 100
            precision mediump float;
            varying vec3 vNormal;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            uniform vec3 uLightPos;
            uniform vec3 uViewPos;
            void main() {
                vec3 normal = normalize(vNormal);
                vec3 lightDir = normalize(uLightPos - vec3(vTexCoord, 0.0));
                float diff = max(dot(normal, lightDir), 0.0);
                gl_FragColor = texture2D(uTexture, vTexCoord) * (0.3 + diff * 0.7);
            }
        """
        
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, vertexShaderCode)
        GLES20.glCompileShader(vertexShader)
        
        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode)
        GLES20.glCompileShader(fragmentShader)
        
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        
        return program
    }
}

data class Vector3(var x: Float, var y: Float, var z: Float)
data class Vector2(var x: Float, var y: Float)

enum class CarModel {
    PLAYER_RED, TRAFFIC_RED, TRAFFIC_BLUE, TRAFFIC_GREEN, TRAFFIC_YELLOW, TRAFFIC_PURPLE
}

class DetailedCar3D(x: Float, y: Float, z: Float, val model: CarModel, vertexCount: Int) {
    val position = Vector3(x, y, z)
    val rotation = Vector3(0f, 0f, 0f)
    val width = 0.65f
    val height = 0.85f
    val length = 1.35f
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val normalBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer
    
    init {
        val vertices = generateDetailedCarGeometry(vertexCount)
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(vertices); position(0) }
        
        val colors = generateCarColors()
        colorBuffer = ByteBuffer.allocateDirect(colors.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(colors); position(0) }
        
        val normals = generateNormals(vertices)
        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(normals); position(0) }
        
        val texCoords = generateTexCoords(vertexCount)
        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(texCoords); position(0) }
    }
    
    fun draw(shaderProgram: Int) {
        GLES20.glPushMatrix()
        GLES20.glTranslatef(position.x, position.y, position.z)
        GLES20.glRotatef(rotation.x, 1f, 0f, 0f)
        GLES20.glRotatef(rotation.y, 0f, 1f, 0f)
        GLES20.glRotatef(rotation.z, 0f, 0f, 1f)
        
        // Draw main body
        drawCarBody()
        
        // Draw windows
        drawWindows()
        
        // Draw lights
        drawHeadlights()
        
        // Draw wheels
        drawWheels()
        
        GLES20.glPopMatrix()
    }
    
    private fun drawCarBody() {
        val (r, g, b) = when (model) {
            CarModel.PLAYER_RED -> Triple(1.0f, 0.4f, 0.0f)
            CarModel.TRAFFIC_RED -> Triple(1.0f, 0.0f, 0.0f)
            CarModel.TRAFFIC_BLUE -> Triple(0.0f, 0.0f, 1.0f)
            CarModel.TRAFFIC_GREEN -> Triple(0.0f, 0.8f, 0.0f)
            CarModel.TRAFFIC_YELLOW -> Triple(1.0f, 1.0f, 0.0f)
            CarModel.TRAFFIC_PURPLE -> Triple(0.8f, 0.0f, 0.8f)
        }
        
        GLES20.glColor4f(r, g, b, 1.0f)
        drawCube(width, height, length)
    }
    
    private fun drawWindows() {
        GLES20.glColor4f(0.3f, 0.7f, 1.0f, 0.6f)
        GLES20.glBegin(GLES20.GL_QUADS)
        // Front window
        GLES20.glVertex3f(-width * 0.4f, height * 0.3f, length * 0.5f)
        GLES20.glVertex3f(width * 0.4f, height * 0.3f, length * 0.5f)
        GLES20.glVertex3f(width * 0.4f, height * 0.8f, length * 0.5f)
        GLES20.glVertex3f(-width * 0.4f, height * 0.8f, length * 0.5f)
        GLES20.glEnd()
    }
    
    private fun drawHeadlights() {
        GLES20.glColor4f(1.0f, 1.0f, 0.8f, 1.0f)
        // Left headlight
        drawSphere(-width * 0.3f, height * 0.2f, length * 0.5f + 0.05f, 0.08f, 8)
        // Right headlight
        drawSphere(width * 0.3f, height * 0.2f, length * 0.5f + 0.05f, 0.08f, 8)
    }
    
    private fun drawWheels() {
        GLES20.glColor4f(0.1f, 0.1f, 0.1f, 1.0f)
        // Front left wheel
        drawSphere(-width * 0.35f, -height * 0.5f, length * 0.3f, 0.12f, 12)
        // Front right wheel
        drawSphere(width * 0.35f, -height * 0.5f, length * 0.3f, 0.12f, 12)
        // Back left wheel
        drawSphere(-width * 0.35f, -height * 0.5f, -length * 0.3f, 0.12f, 12)
        // Back right wheel
        drawSphere(width * 0.35f, -height * 0.5f, -length * 0.3f, 0.12f, 12)
    }
    
    private fun drawCube(w: Float, h: Float, l: Float) {
        val hw = w / 2
        val hh = h / 2
        val hl = l / 2
        
        GLES20.glBegin(GLES20.GL_QUADS)
        // Front
        GLES20.glVertex3f(-hw, -hh, hl)
        GLES20.glVertex3f(hw, -hh, hl)
        GLES20.glVertex3f(hw, hh, hl)
        GLES20.glVertex3f(-hw, hh, hl)
        // Back
        GLES20.glVertex3f(-hw, -hh, -hl)
        GLES20.glVertex3f(-hw, hh, -hl)
        GLES20.glVertex3f(hw, hh, -hl)
        GLES20.glVertex3f(hw, -hh, -hl)
        // Top
        GLES20.glVertex3f(-hw, hh, -hl)
        GLES20.glVertex3f(-hw, hh, hl)
        GLES20.glVertex3f(hw, hh, hl)
        GLES20.glVertex3f(hw, hh, -hl)
        // Bottom
        GLES20.glVertex3f(-hw, -hh, -hl)
        GLES20.glVertex3f(hw, -hh, -hl)
        GLES20.glVertex3f(hw, -hh, hl)
        GLES20.glVertex3f(-hw, -hh, hl)
        // Right
        GLES20.glVertex3f(hw, -hh, -hl)
        GLES20.glVertex3f(hw, hh, -hl)
        GLES20.glVertex3f(hw, hh, hl)
        GLES20.glVertex3f(hw, -hh, hl)
        // Left
        GLES20.glVertex3f(-hw, -hh, -hl)
        GLES20.glVertex3f(-hw, -hh, hl)
        GLES20.glVertex3f(-hw, hh, hl)
        GLES20.glVertex3f(-hw, hh, -hl)
        GLES20.glEnd()
    }
    
    private fun drawSphere(cx: Float, cy: Float, cz: Float, radius: Float, resolution: Int) {
        for (i in 0 until resolution) {
            val lat0 = PI.toFloat() * (-0.5f + i.toFloat() / resolution)
            val lat1 = PI.toFloat() * (-0.5f + (i + 1).toFloat() / resolution)
            
            GLES20.glBegin(GLES20.GL_QUAD_STRIP)
            for (j in 0 until resolution + 1) {
                val lng = 2 * PI.toFloat() * j.toFloat() / resolution
                
                val x0 = kotlin.math.cos(lat0.toDouble()).toFloat() * kotlin.math.cos(lng.toDouble()).toFloat()
                val y0 = kotlin.math.sin(lat0.toDouble()).toFloat()
                val z0 = kotlin.math.cos(lat0.toDouble()).toFloat() * kotlin.math.sin(lng.toDouble()).toFloat()
                
                val x1 = kotlin.math.cos(lat1.toDouble()).toFloat() * kotlin.math.cos(lng.toDouble()).toFloat()
                val y1 = kotlin.math.sin(lat1.toDouble()).toFloat()
                val z1 = kotlin.math.cos(lat1.toDouble()).toFloat() * kotlin.math.sin(lng.toDouble()).toFloat()
                
                GLES20.glVertex3f(cx + x0 * radius, cy + y0 * radius, cz + z0 * radius)
                GLES20.glVertex3f(cx + x1 * radius, cy + y1 * radius, cz + z1 * radius)
            }
            GLES20.glEnd()
        }
    }
    
    private fun generateDetailedCarGeometry(vertexCount: Int): FloatArray {
        val vertices = FloatArray(vertexCount * 3)
        for (i in 0 until vertexCount) {
            val angle = (i.toFloat() / vertexCount) * 2 * PI.toFloat()
            vertices[i * 3] = kotlin.math.cos(angle.toDouble()).toFloat() * width
            vertices[i * 3 + 1] = height
            vertices[i * 3 + 2] = kotlin.math.sin(angle.toDouble()).toFloat() * length
        }
        return vertices
    }
    
    private fun generateCarColors(): FloatArray {
        val vertices = 256
        val colors = FloatArray(vertices * 4)
        val (r, g, b) = when (model) {
            CarModel.PLAYER_RED -> Triple(1.0f, 0.4f, 0.0f)
            CarModel.TRAFFIC_RED -> Triple(1.0f, 0.0f, 0.0f)
            CarModel.TRAFFIC_BLUE -> Triple(0.0f, 0.0f, 1.0f)
            CarModel.TRAFFIC_GREEN -> Triple(0.0f, 0.8f, 0.0f)
            CarModel.TRAFFIC_YELLOW -> Triple(1.0f, 1.0f, 0.0f)
            CarModel.TRAFFIC_PURPLE -> Triple(0.8f, 0.0f, 0.8f)
        }
        for (i in 0 until vertices) {
            colors[i * 4] = r
            colors[i * 4 + 1] = g
            colors[i * 4 + 2] = b
            colors[i * 4 + 3] = 1.0f
        }
        return colors
    }
    
    private fun generateNormals(vertices: FloatArray): FloatArray {
        return FloatArray(vertices.size) { i -> if (i % 3 == 1) 1.0f else 0.0f }
    }
    
    private fun generateTexCoords(vertexCount: Int): FloatArray {
        val texCoords = FloatArray(vertexCount * 2)
        for (i in 0 until vertexCount) {
            texCoords[i * 2] = i.toFloat() / vertexCount
            texCoords[i * 2 + 1] = 0.5f
        }
        return texCoords
    }
}

class CoinParticle(x: Float, y: Float, z: Float, val glowing: Boolean) {
    val position = Vector3(x, y, z)
    var rotation = Vector3(0f, 0f, 0f)
    
    fun draw(shaderProgram: Int) {
        GLES20.glPushMatrix()
        GLES20.glTranslatef(position.x, position.y, position.z)
        GLES20.glRotatef(rotation.y, 0f, 1f, 0f)
        
        GLES20.glColor4f(1.0f, 0.84f, 0.0f, 1.0f)
        
        // Draw coin
        GLES20.glBegin(GLES20.GL_TRIANGLE_FAN)
        for (i in 0..32) {
            val angle = (i.toFloat() / 32) * 2 * PI.toFloat()
            GLES20.glVertex3f(
                kotlin.math.cos(angle.toDouble()).toFloat() * 0.25f,
                kotlin.math.sin(angle.toDouble()).toFloat() * 0.1f,
                0f
            )
        }
        GLES20.glEnd()
        
        // Draw glow ring
        if (glowing) {
            GLES20.glColor4f(1.0f, 1.0f, 0.0f, 0.3f)
            GLES20.glBegin(GLES20.GL_LINE_LOOP)
            for (i in 0..32) {
                val angle = (i.toFloat() / 32) * 2 * PI.toFloat()
                GLES20.glVertex3f(
                    kotlin.math.cos(angle.toDouble()).toFloat() * 0.35f,
                    0f,
                    kotlin.math.sin(angle.toDouble()).toFloat() * 0.35f
                )
            }
            GLES20.glEnd()
        }
        
        GLES20.glPopMatrix()
    }
}

class Particle(x: Float, y: Float, z: Float, vx: Float, vy: Float, vz: Float, r: Float, g: Float, b: Float, val lifespan: Int) {
    val position = Vector3(x, y, z)
    val velocity = Vector3(vx, vy, vz)
    val color = floatArrayOf(r, g, b)
    var age = 0
    
    fun update(gameSpeed: Float) {
        position.x += velocity.x
        position.y += velocity.y
        position.z += velocity.z + gameSpeed
        velocity.y -= 0.02f // gravity
        age++
    }
    
    fun draw(shaderProgram: Int) {
        GLES20.glPushMatrix()
        GLES20.glTranslatef(position.x, position.y, position.z)
        
        val alpha = 1.0f - (age.toFloat() / lifespan)
        GLES20.glColor4f(color[0], color[1], color[2], alpha)
        
        GLES20.glBegin(GLES20.GL_QUADS)
        val size = 0.05f
        GLES20.glVertex3f(-size, -size, 0f)
        GLES20.glVertex3f(size, -size, 0f)
        GLES20.glVertex3f(size, size, 0f)
        GLES20.glVertex3f(-size, size, 0f)
        GLES20.glEnd()
        
        GLES20.glPopMatrix()
    }
    
    fun isDead(): Boolean = age >= lifespan
}

class RoadSegment3D(val position: Float, val vertexCount: Int) {
    private val vertices: FloatArray = FloatArray(vertexCount * 3)
    
    init {
        for (i in 0 until vertexCount) {
            val angle = (i.toFloat() / vertexCount) * 2 * PI.toFloat()
            vertices[i * 3] = kotlin.math.cos(angle.toDouble()).toFloat() * 2f
            vertices[i * 3 + 1] = 0f
            vertices[i * 3 + 2] = kotlin.math.sin(angle.toDouble()).toFloat() * 2f
        }
    }
}