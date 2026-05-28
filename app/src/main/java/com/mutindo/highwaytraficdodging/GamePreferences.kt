package com.mutindo.highwaytraficdodging

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    companion object {
        const val HIGH_SCORE_KEY = "high_score"
        const val COINS_KEY = "coins"
        const val SELECTED_CAR_KEY = "selected_car"
        const val CAR_UNLOCKED_PREFIX = "car_unlocked_"
    }

    fun getHighScore(): Int {
        return sharedPreferences.getInt(HIGH_SCORE_KEY, 0)
    }

    fun setHighScore(score: Int) {
        if (score > getHighScore()) {
            sharedPreferences.edit().putInt(HIGH_SCORE_KEY, score).apply()
        }
    }

    fun getCoins(): Int {
        return sharedPreferences.getInt(COINS_KEY, 0)
    }

    fun addCoins(amount: Int) {
        val current = getCoins()
        sharedPreferences.edit().putInt(COINS_KEY, current + amount).apply()
    }

    fun deductCoins(amount: Int): Boolean {
        val current = getCoins()
        return if (current >= amount) {
            sharedPreferences.edit().putInt(COINS_KEY, current - amount).apply()
            true
        } else {
            false
        }
    }

    fun getSelectedCar(): String {
        return sharedPreferences.getString(SELECTED_CAR_KEY, "car_1") ?: "car_1"
    }

    fun setSelectedCar(carId: String) {
        sharedPreferences.edit().putString(SELECTED_CAR_KEY, carId).apply()
    }

    fun isCarUnlocked(carId: String): Boolean {
        return sharedPreferences.getBoolean("${CAR_UNLOCKED_PREFIX}${carId}", carId == "car_1")
    }

    fun unlockCar(carId: String) {
        sharedPreferences.edit().putBoolean("${CAR_UNLOCKED_PREFIX}${carId}", true).apply()
    }
}
