package com.halfback.breakout_quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.halfback.breakout_quiz.utils.WindowInsetsUtil

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        WindowInsetsUtil.applySafePadding(findViewById(R.id.rootLayout))

        findViewById<Button>(R.id.startButton).setOnClickListener {
            // ジャンル選択画面へ
            val intent = Intent(this, GenreSelectActivity::class.java)
            startActivity(intent)
        }

    }
}
