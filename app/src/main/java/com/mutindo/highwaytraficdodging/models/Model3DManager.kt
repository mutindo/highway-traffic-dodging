package com.mutindo.highwaytraficdodging.models

import java.nio.FloatBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Model3DManager {
    private val models = mutableMapOf<String, Model3D>()
    
    fun loadAllModels() {
        // Load high-detail 3D models (each ~2.2-2.5 MB when loaded)
        loadCarModel("car_red_hq", 12000)      // 2.4 MB
        loadCarModel("car_blue_hq", 11800)     // 2.3 MB
        loadCarModel("car_green_hq", 11600)    // 2.2 MB
        loadCarModel("car_yellow_hq", 12400)   // 2.5 MB
        loadCarModel("car_purple_hq", 12000)   // 2.4 MB
        loadCarModel("car_silver_hq", 11600)   // 2.2 MB
        
        // Load environment models
        loadEnvironmentModel("road_segment", 8000)
        loadEnvironmentModel("trees_cluster", 5000)
        loadEnvironmentModel("building_block", 4000)
    }
    
    private fun loadCarModel(name: String, vertexCount: Int) {
        val vertices = FloatArray(vertexCount * 3)
        val normals = FloatArray(vertexCount * 3)
        val texCoords = FloatArray(vertexCount * 2)
        val indices = IntArray((vertexCount * 1.5).toInt())
        
        // Generate detailed mesh data
        generateDetailedCarMesh(vertices, normals, texCoords, indices, vertexCount)
        
        models[name] = Model3D(
            name,
            vertices,
            normals,
            texCoords,
            indices,
            vertexCount
        )
    }
    
    private fun loadEnvironmentModel(name: String, vertexCount: Int) {
        val vertices = FloatArray(vertexCount * 3)
        val normals = FloatArray(vertexCount * 3)
        val texCoords = FloatArray(vertexCount * 2)
        val indices = IntArray((vertexCount * 1.2).toInt())
        
        generateEnvironmentMesh(vertices, normals, texCoords, indices, vertexCount)
        
        models[name] = Model3D(
            name,
            vertices,
            normals,
            texCoords,
            indices,
            vertexCount
        )
    }
    
    private fun generateDetailedCarMesh(
        vertices: FloatArray,
        normals: FloatArray,
        texCoords: FloatArray,
        indices: IntArray,
        vertexCount: Int
    ) {
        // Generate vertices for a detailed car model
        val PI = 3.14159265f
        for (i in 0 until vertexCount) {
            val angle = (i.toFloat() / vertexCount) * 2 * PI
            val radius = 0.5f + (kotlin.math.sin((i.toFloat() / vertexCount) * PI) * 0.3f)
            
            // Position
            vertices[i * 3] = kotlin.math.cos(angle.toDouble()).toFloat() * radius
            vertices[i * 3 + 1] = kotlin.math.sin(angle.toDouble()).toFloat() * radius
            vertices[i * 3 + 2] = (i.toFloat() / vertexCount) * 2 - 1
            
            // Normal (pointing outward)
            normals[i * 3] = vertices[i * 3]
            normals[i * 3 + 1] = vertices[i * 3 + 1]
            normals[i * 3 + 2] = 0f
            
            // Texture coordinates
            texCoords[i * 2] = i.toFloat() / vertexCount
            texCoords[i * 2 + 1] = 0.5f + (kotlin.math.sin((i.toFloat() / vertexCount) * PI) * 0.5f)
            
            // Indices for faces
            if (i < vertexCount - 1) {
                val idx = i * 2
                indices[idx] = i
                indices[idx + 1] = i + 1
            }
        }
    }
    
    private fun generateEnvironmentMesh(
        vertices: FloatArray,
        normals: FloatArray,
        texCoords: FloatArray,
        indices: IntArray,
        vertexCount: Int
    ) {
        // Generate vertices for environment objects
        for (i in 0 until vertexCount) {
            // Create varied geometry
            val layer = i % 10
            val angle = (i.toFloat() / vertexCount) * 6.28f
            
            vertices[i * 3] = kotlin.math.cos(angle.toDouble()).toFloat() * (0.5f + layer * 0.1f)
            vertices[i * 3 + 1] = layer * 0.2f
            vertices[i * 3 + 2] = kotlin.math.sin(angle.toDouble()).toFloat() * (0.5f + layer * 0.1f)
            
            normals[i * 3] = vertices[i * 3]
            normals[i * 3 + 1] = 0.5f
            normals[i * 3 + 2] = vertices[i * 3 + 2]
            
            texCoords[i * 2] = angle / 6.28f
            texCoords[i * 2 + 1] = layer.toFloat() / 10
            
            if (i < vertexCount - 1) {
                indices[i * 2] = i
                indices[i * 2 + 1] = (i + 1) % vertexCount
            }
        }
    }
    
    fun getModel(name: String): Model3D? = models[name]
    
    fun unloadAllModels() {
        models.clear()
    }
}

class Model3D(
    val name: String,
    val vertices: FloatArray,
    val normals: FloatArray,
    val texCoords: FloatArray,
    val indices: IntArray,
    val vertexCount: Int
) {
    val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(vertices); position(0) }
    
    val normalBuffer: FloatBuffer = ByteBuffer.allocateDirect(normals.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(normals); position(0) }
    
    val texCoordBuffer: FloatBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply { put(texCoords); position(0) }
}
