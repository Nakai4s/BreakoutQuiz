package com.example.breakout_quiz.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.breakout_quiz.R

/**
 * BGM・SEを管理する
 */
object SoundManager {

    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // 効果音ファイルを res/raw に置き、ここで読み込む
        soundMap["paddle"] = soundPool.load(context, R.raw.se_paddle, 1)
        soundMap["true"] = soundPool.load(context, R.raw.se_true, 1)
        soundMap["false"] = soundPool.load(context, R.raw.se_false, 1)

        isInitialized = true
    }

    fun play(key: String) {
        val soundId = soundMap[key] ?: return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        if (isInitialized) {
            soundPool.release()
            isInitialized = false
        }
    }
}
