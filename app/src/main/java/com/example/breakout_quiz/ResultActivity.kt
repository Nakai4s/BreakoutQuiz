package com.example.breakout_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val score = intent.getIntExtra("score", 0)
        val time = intent.getLongExtra("elapsed_time", 0)

        ScoreManager.saveScore(this, score, time)

        findViewById<TextView>(R.id.result_text).text = "スコア：${score}問正解"
        findViewById<TextView>(R.id.time_text).text = "クリア時間：${time}秒"

        displayRanking()

        findViewById<Button>(R.id.retryButton).setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.titleButton).setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }
    }

    private fun displayRanking() {
        val rankingLayout = findViewById<LinearLayout>(R.id.ranking_layout)
        rankingLayout.removeAllViews()

        val scores = ScoreManager.getScores(this)
        scores.forEachIndexed { index, entry ->
            val view = TextView(this).apply {
                text = "${index + 1}位：${entry.score}問（${entry.time}秒）"
                textSize = 16f
            }
            rankingLayout.addView(view)
        }
    }

}
