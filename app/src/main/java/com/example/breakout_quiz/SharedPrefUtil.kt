package com.example.breakout_quiz

import android.content.Context
import android.content.SharedPreferences

object SharedPrefUtil {
    private const val PREF_NAME = "BreakoutQuizPrefs"

    fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getInt(context: Context, key: String, default: Int): Int =
        getPrefs(context).getInt(key, default)

    fun setInt(context: Context, key: String, value: Int) {
        getPrefs(context).edit().putInt(key, value).apply()
    }
}
