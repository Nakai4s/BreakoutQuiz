package com.example.breakout_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.breakout_quiz.utils.WindowInsetsUtil

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        WindowInsetsUtil.applySafePadding(findViewById(R.id.rootLayout))

        val score = intent.getIntExtra("score", 0)
        val time = intent.getLongExtra("elapsed_time", 0)
        val genre = intent.getStringExtra("genre")

        ScoreManager.saveScore(this, score, time)

        findViewById<TextView>(R.id.result_text).text = "スコア：${score}問正解"

        findViewById<Button>(R.id.retryButton).setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("genre", genre)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.titleButton).setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }
    }
}
