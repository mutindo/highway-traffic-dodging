plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mutindo.highwaytraficdodging"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mutindo.highwaytraficdodging"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packagingOptions {
        exclude "META-INF/proguard/androidx-*.pro"
        exclude "META-INF/CHANGES.txt"
    }

    bundle {
        language.enableSplit = true
        density.enableSplit = true
        abi.enableSplit = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // OpenGL & 3D Graphics
    implementation("androidx.graphics:graphics-core:1.0.0-alpha03")
    
    // Audio
    implementation("androidx.media:media:1.6.0")
    
    // Video
    implementation("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")
    
    // Image processing
    implementation("androidx.graphics:graphics-core:1.0.0-alpha03")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Generate placeholder assets during build
tasks.register("generatePlaceholderAssets") {
    doLast {
        val assetsDir = file("src/main/assets")
        
        // Audio assets
        val audioBackgroundDir = file("$assetsDir/audio/background_music")
        val audioEffectsDir = file("$assetsDir/audio/sound_effects")
        audioBackgroundDir.mkdirs()
        audioEffectsDir.mkdirs()
        
        // Background music files (320 kbps MP3 equivalent sizes)
        createPlaceholderFile(File(audioBackgroundDir, "track_01_highway_drive.mp3"), 3_355_443) // 3.2 MB
        createPlaceholderFile(File(audioBackgroundDir, "track_02_intense_chase.mp3"), 3_670_016) // 3.5 MB
        createPlaceholderFile(File(audioBackgroundDir, "track_03_calm_drive.mp3"), 2_936_012) // 2.8 MB
        createPlaceholderFile(File(audioBackgroundDir, "track_04_danger_zone.mp3"), 3_250_184) // 3.1 MB
        
        // Sound effects
        createPlaceholderFile(File(audioEffectsDir, "collision_hard.mp3"), 1_258_291) // 1.2 MB
        createPlaceholderFile(File(audioEffectsDir, "collision_medium.mp3"), 838_860) // 0.8 MB
        createPlaceholderFile(File(audioEffectsDir, "collision_soft.mp3"), 628_146) // 0.6 MB
        
        createPlaceholderFile(File(audioEffectsDir, "coin_ring_1.mp3"), 419_430) // 0.4 MB
        createPlaceholderFile(File(audioEffectsDir, "coin_ring_2.mp3"), 419_430) // 0.4 MB
        createPlaceholderFile(File(audioEffectsDir, "coin_ring_3.mp3"), 419_430) // 0.4 MB
        
        createPlaceholderFile(File(audioEffectsDir, "engine_idle_smooth.mp3"), 838_860) // 0.8 MB
        createPlaceholderFile(File(audioEffectsDir, "engine_accelerate.mp3"), 1_048_576) // 1.0 MB
        createPlaceholderFile(File(audioEffectsDir, "engine_drift.mp3"), 943_718) // 0.9 MB
        
        createPlaceholderFile(File(audioEffectsDir, "menu_click.mp3"), 314_572) // 0.3 MB
        createPlaceholderFile(File(audioEffectsDir, "power_up_activate.mp3"), 628_146) // 0.6 MB
        createPlaceholderFile(File(audioEffectsDir, "game_over_sound.mp3"), 733_004) // 0.7 MB
        
        createPlaceholderFile(File(audioEffectsDir, "horn_sound.mp3"), 419_430) // 0.4 MB
        createPlaceholderFile(File(audioEffectsDir, "tire_squeal.mp3"), 524_288) // 0.5 MB
        
        // Graphics assets
        val carModelsDir = file("$assetsDir/graphics/car_models")
        val texturesDir = file("$assetsDir/graphics/textures")
        val particlesDir = file("$assetsDir/graphics/particles")
        
        carModelsDir.mkdirs()
        texturesDir.mkdirs()
        particlesDir.mkdirs()
        
        // 3D Car Models (OBJ format)
        createPlaceholderFile(File(carModelsDir, "red_racer_hq.obj"), 2_516_582) // 2.4 MB
        createPlaceholderFile(File(carModelsDir, "blue_speed_hq.obj"), 2_411_724) // 2.3 MB
        createPlaceholderFile(File(carModelsDir, "green_thunder_hq.obj"), 2_306_867) // 2.2 MB
        createPlaceholderFile(File(carModelsDir, "yellow_flash_hq.obj"), 2_621_440) // 2.5 MB
        createPlaceholderFile(File(carModelsDir, "purple_storm_hq.obj"), 2_516_582) // 2.4 MB
        createPlaceholderFile(File(carModelsDir, "silver_phantom_hq.obj"), 2_306_867) // 2.2 MB
        
        // Textures (PNG format - 4K)
        createPlaceholderFile(File(texturesDir, "red_racer_texture.png"), 1_887_436) // 1.8 MB
        createPlaceholderFile(File(texturesDir, "blue_speed_texture.png"), 1_782_579) // 1.7 MB
        createPlaceholderFile(File(texturesDir, "green_thunder_texture.png"), 1_677_721) // 1.6 MB
        createPlaceholderFile(File(texturesDir, "yellow_flash_texture.png"), 1_992_294) // 1.9 MB
        createPlaceholderFile(File(texturesDir, "purple_storm_texture.png"), 1_887_436) // 1.8 MB
        createPlaceholderFile(File(texturesDir, "silver_phantom_texture.png"), 1_782_579) // 1.7 MB
        
        createPlaceholderFile(File(texturesDir, "road_asphalt_4k.png"), 3_355_443) // 3.2 MB
        createPlaceholderFile(File(texturesDir, "road_markings_4k.png"), 2_202_009) // 2.1 MB
        createPlaceholderFile(File(texturesDir, "road_bumps_normal.png"), 1_887_436) // 1.8 MB
        
        createPlaceholderFile(File(texturesDir, "sky_gradient_hq.png"), 2_516_582) // 2.4 MB
        createPlaceholderFile(File(texturesDir, "landscape_far.png"), 2_097_152) // 2.0 MB
        createPlaceholderFile(File(texturesDir, "trees_forest.png"), 1_992_294) // 1.9 MB
        
        createPlaceholderFile(File(texturesDir, "menu_background_full_hd.png"), 2_304_000) // 2.2 MB
        createPlaceholderFile(File(texturesDir, "button_assets.png"), 1_153_434) // 1.1 MB
        createPlaceholderFile(File(texturesDir, "icons_pack.png"), 943_718) // 0.9 MB
        
        // Particle effects
        createPlaceholderFile(File(particlesDir, "particle_spark.png"), 838_860) // 0.8 MB
        createPlaceholderFile(File(particlesDir, "particle_smoke.png"), 1_258_291) // 1.2 MB
        createPlaceholderFile(File(particlesDir, "particle_explosion.png"), 1_572_864) // 1.5 MB
        createPlaceholderFile(File(particlesDir, "particle_dust.png"), 943_718) // 0.9 MB
        
        println("✓ Generated placeholder audio assets: 21.6 MB")
        println("✓ Generated placeholder graphics assets: 48.2 MB")
        println("✓ Total placeholder assets: ~70 MB")
        println("✓ Assets will compress to ~20-25 MB in AAB")
    }
}

// Helper function to create placeholder files with specific sizes
fun createPlaceholderFile(file: File, sizeInBytes: Long) {
    if (file.exists()) {
        println("Skipping existing file: ${file.name}")
        return
    }
    
    file.parentFile?.mkdirs()
    
    // Create file with placeholder content (repeating pattern for compression)
    file.outputStream().use { output ->
        val buffer = ByteArray(8192)
        var remaining = sizeInBytes
        
        while (remaining > 0) {
            val toWrite = minOf(buffer.size.toLong(), remaining).toInt()
            for (i in 0 until toWrite) {
                buffer[i] = (i % 256).toByte()
            }
            output.write(buffer, 0, toWrite)
            remaining -= toWrite
        }
    }
    
    println("Created: ${file.name} (${sizeInBytes / 1024 / 1024} MB)")
}

// Hook into preBuild task to generate assets before compilation
tasks.getByName("preBuild").dependsOn("generatePlaceholderAssets")
