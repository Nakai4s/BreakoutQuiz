package com.example.breakout_quiz

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var editPaddleWidth: EditText
    private lateinit var editTimeLimit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editPaddleWidth = findViewById(R.id.edit_paddle_width)
        editTimeLimit = findViewById(R.id.edit_time_limit)

        // 初期値を読み込み表示
        editPaddleWidth.setText(SharedPrefUtil.getInt(this, PreferenceKeys.PADDLE_WIDTH, 300).toString())
        editTimeLimit.setText(SharedPrefUtil.getInt(this, PreferenceKeys.TIME_LIMIT, 60).toString())

        findViewById<Button>(R.id.save_button).setOnClickListener {
            SharedPrefUtil.setInt(this, PreferenceKeys.PADDLE_WIDTH, editPaddleWidth.text.toString().toIntOrNull() ?: 300)
            SharedPrefUtil.setInt(this, PreferenceKeys.TIME_LIMIT, editTimeLimit.text.toString().toIntOrNull() ?: 60)
            finish()
        }
    }
}
