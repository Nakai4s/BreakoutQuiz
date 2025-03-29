package com.example.breakout_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        findViewById<Button>(R.id.retryButton).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.titleButton).setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

        val score = intent.getIntExtra("score", 0)
        val textView = findViewById<TextView>(R.id.result_text)
        textView.text = "スコア：$score 問正解"
    }
}
