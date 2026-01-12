package com.hnalovski.triviaapp.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Activity) {
    private val preferences: SharedPreferences = context.getPreferences(Context.MODE_PRIVATE)

    fun savedHighestScore(score: Int) {
        val lastScore = preferences.getInt(HIGHEST_SCORE, 0)

        if (score > lastScore) {
            preferences.edit().putInt(HIGHEST_SCORE, score).apply()
        }
    }

    val highestScore: Int
        get() = preferences.getInt(HIGHEST_SCORE, 0)


    var state: Int
        get() = preferences.getInt(STATE, 0)
        set(index) {
            preferences.edit().putInt(STATE, index).apply()
        }

    companion object {
        const val HIGHEST_SCORE: String = "Highest Score"
        const val STATE: String = "trivia_state"
    }
}
