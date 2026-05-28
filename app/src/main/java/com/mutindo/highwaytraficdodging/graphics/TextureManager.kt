package com.mutindo.highwaytraficdodging.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class TextureManager(private val context: Context) {
    private val textures = mutableMapOf<String, Int>()
    private val cachedBitmaps = mutableMapOf<String, Bitmap>()
    
    // Load all 4K textures
    fun loadAllTextures() {
        val textureAssets = listOf(
            // Car textures (6 cars × ~1.8 MB each)
            Pair("car_red", 1800000),
            Pair("car_blue", 1700000),
            Pair("car_green", 1600000),
            Pair("car_yellow", 1900000),
            Pair("car_purple", 1800000),
            Pair("car_silver", 1700000),
            
            // Road textures (3 × 2-3 MB each)
            Pair("road_asphalt", 3200000),
            Pair("road_markings", 2100000),
            Pair("road_normal_map", 1800000),
            
            // Environment (3 × 1.9-2.4 MB)
            Pair("sky_gradient", 2400000),
            Pair("landscape_far", 2000000),
            Pair("trees_forest", 1900000),
            
            // UI (3 × 0.9-2.2 MB)
            Pair("menu_background", 2200000),
            Pair("button_assets", 1100000),
            Pair("icons_pack", 900000)
        )
        
        // Simulate loading high-resolution textures
        for ((textureName, _) in textureAssets) {
            textures[textureName] = generateTextureId(textureName)
        }
    }
    
    // Load high-quality particle textures
    fun loadParticleTextures() {
        val particleAssets = listOf(
            "particle_spark",      // 800 KB
            "particle_smoke",      // 1.2 MB
            "particle_explosion",  // 1.5 MB
            "particle_dust"        // 900 KB
        )
        
        for (textureName in particleAssets) {
            textures[textureName] = generateTextureId(textureName)
        }
    }
    
    // Generate procedural texture data (simulates loading actual texture files)
    private fun generateTextureId(name: String): Int {
        val bitmap = when (name) {
            "car_red" -> createCarTexture(255, 100, 100)
            "car_blue" -> createCarTexture(100, 100, 255)
            "car_green" -> createCarTexture(100, 200, 100)
            "car_yellow" -> createCarTexture(255, 255, 100)
            "car_purple" -> createCarTexture(200, 100, 200)
            "car_silver" -> createCarTexture(200, 200, 200)
            "road_asphalt" -> createRoadTexture()
            "sky_gradient" -> createSkyTexture()
            "menu_background" -> createMenuTexture()
            else -> createDefaultTexture()
        }
        cachedBitmaps[name] = bitmap
        return name.hashCode()
    }
    
    private fun createCarTexture(r: Int, g: Int, b: Int): Bitmap {
        // Create 2048x2048 texture (4K)
        val bitmap = Bitmap.createBitmap(2048, 2048, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(2048 * 2048)
        
        // Fill with gradient color
        for (i in 0 until 2048 * 2048) {
            val row = i / 2048
            val col = i % 2048
            val shade = (row * 255) / 2048
            pixels[i] = (0xFF shl 24) or ((r * shade / 255) shl 16) or ((g * shade / 255) shl 8) or (b * shade / 255)
        }
        
        bitmap.setPixels(pixels, 0, 2048, 0, 0, 2048, 2048)
        return bitmap
    }
    
    private fun createRoadTexture(): Bitmap {
        // Create detailed road texture (4K)
        val bitmap = Bitmap.createBitmap(4096, 2048, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(4096 * 2048)
        
        // Asphalt pattern with marks
        for (i in 0 until 4096 * 2048) {
            val row = i / 4096
            val col = i % 4096
            
            val baseColor = if ((row / 128) % 2 == 0) 0xFF333333.toInt() else 0xFF444444.toInt()
            val withMarks = if ((col / 256) % 2 == 0) 0xFFFFFF00.toInt() else baseColor
            pixels[i] = withMarks
        }
        
        bitmap.setPixels(pixels, 0, 4096, 0, 0, 4096, 2048)
        return bitmap
    }
    
    private fun createSkyTexture(): Bitmap {
        // Create sky gradient texture
        val bitmap = Bitmap.createBitmap(2048, 2048, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(2048 * 2048)
        
        for (i in 0 until 2048 * 2048) {
            val row = i / 2048
            val col = i % 2048
            
            // Blue sky gradient from bottom to top
            val intensity = 255 - (row * 128) / 2048
            val r = 100 + (intensity / 4)
            val g = 150 + (intensity / 3)
            val b = 255
            
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        
        bitmap.setPixels(pixels, 0, 2048, 0, 0, 2048, 2048)
        return bitmap
    }
    
    private fun createMenuTexture(): Bitmap {
        // Create high-resolution menu background
        val bitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(1920 * 1080)
        
        for (i in 0 until 1920 * 1080) {
            val row = i / 1920
            val col = i % 1920
            
            // Dark gradient background
            val intensity = 255 - (row * 200) / 1080
            pixels[i] = (0xFF shl 24) or (intensity shl 16) or (intensity shl 8) or intensity
        }
        
        bitmap.setPixels(pixels, 0, 1920, 0, 0, 1920, 1080)
        return bitmap
    }
    
    private fun createDefaultTexture(): Bitmap {
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(512 * 512) { i ->
            (0xFF shl 24) or (200 shl 16) or (200 shl 8) or 200
        }
        bitmap.setPixels(pixels, 0, 512, 0, 0, 512, 512)
        return bitmap
    }
    
    fun getTexture(name: String): Int = textures[name] ?: -1
    
    fun unloadTexture(name: String) {
        textures.remove(name)
        cachedBitmaps[name]?.recycle()
        cachedBitmaps.remove(name)
    }
    
    fun unloadAllTextures() {
        cachedBitmaps.values.forEach { it.recycle() }
        cachedBitmaps.clear()
        textures.clear()
    }
}
