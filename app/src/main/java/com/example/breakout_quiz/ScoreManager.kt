package com.example.breakout_quiz

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ScoreManager {
    private const val PREF_NAME = "BreakoutQuizScores"
    private const val KEY_SCORES = "score_list"
    private const val MAX_ENTRIES = 10

    data class ScoreEntry(val score: Int, val time: Long, val timestamp: Long)

    fun saveScore(context: Context, score: Int, time: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val list = getScores(context).toMutableList()
        list.add(ScoreEntry(score, time, System.currentTimeMillis()))
        val sorted = list.sortedByDescending { it.score }.take(MAX_ENTRIES)
        val json = Gson().toJson(sorted)
        prefs.edit().putString(KEY_SCORES, json).apply()
    }

    fun getScores(context: Context): List<ScoreEntry> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SCORES, "[]")
        val type = object : TypeToken<List<ScoreEntry>>() {}.type
        return Gson().fromJson(json, type)
    }
}
