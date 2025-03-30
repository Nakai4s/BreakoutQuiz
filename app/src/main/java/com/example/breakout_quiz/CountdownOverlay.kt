package com.example.breakout_quiz

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView

class CountdownOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val countdownText: TextView
    private var countdownTime = 3
    private val handler = Handler(Looper.getMainLooper())

    init {
        LayoutInflater.from(context).inflate(R.layout.view_countdown_overlay, this, true)
        countdownText = findViewById(R.id.countdown_text)
    }

    fun startCountdown(onFinish: () -> Unit) {
        GameView.isCountdownActive = true
        this.visibility = VISIBLE
        countdownTime = 3
        updateCountdown(onFinish)
    }

    private fun updateCountdown(onFinish: () -> Unit) {
        countdownText.text = countdownTime.toString()
        if (countdownTime > 0) {
            handler.postDelayed({
                countdownTime--
                updateCountdown(onFinish)
            }, 1000)
        } else {
            this.visibility = GONE
            GameView.isCountdownActive = false
            onFinish()
        }
    }
}
