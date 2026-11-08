package com.duy.skydodger.data

import android.content.Context
import androidx.core.content.edit

/**
 * Best score persistence. SharedPreferences is enough for a single integer and
 * keeps the game completely offline - no database, no network, no accounts.
 */
class ScoreStore(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun bestScore(): Int = preferences.getInt(KEY_BEST_SCORE, 0)

    /** Stores [score] if it beats the current best and returns the resulting best. */
    fun submit(score: Int): Int {
        val current = bestScore()
        if (score <= current) return current
        preferences.edit { putInt(KEY_BEST_SCORE, score) }
        return score
    }

    private companion object {
        const val PREFERENCES_NAME = "sky_dodger_scores"
        const val KEY_BEST_SCORE = "best_score"
    }
}
