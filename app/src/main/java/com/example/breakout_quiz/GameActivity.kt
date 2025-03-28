package com.example.breakout_quiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.breakout_quiz.utils.WindowInsetsUtil

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var countdownOverlay: CountdownOverlay
    private lateinit var answerButton: Button
    private lateinit var choiceLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val rootView = findViewById<View>(R.id.rootLayout) // ルートレイアウトのID
        WindowInsetsUtil.applySafePadding(rootView)

        /*
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            window.attributes = lp
        }
        findViewById<Button>(R.id.endGameButton).setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java)
            startActivity(intent)
            finish()
        }*/

        gameView = findViewById(R.id.game_view)
        countdownOverlay = findViewById(R.id.countdown_overlay)
        answerButton = findViewById(R.id.answer_button)
        choiceLayout = findViewById(R.id.choice_layout)

        answerButton.setOnClickListener {
            enterQuizMode()
        }

        startCountdown()
    }

    private fun startCountdown() {
        countdownOverlay.startCountdown {
            gameView.startGame() // カウント終了後にゲーム開始
        }
    }

    /**
     * クイズ解答モードに切り替え、選択肢を表示する
     */
    private fun enterQuizMode() {
        gameView.isInQuizMode = true
        choiceLayout.visibility = View.VISIBLE
        answerButton.isEnabled = false

        showChoices(listOf("あ", "い", "う", "え")) // 仮データ
    }

    /**
     * 4択ボタンを表示して選択可能にする
     */
    private fun showChoices(choices: List<String>) {
        choiceLayout.removeAllViews()

        for (choice in choices) {
            val button = Button(this).apply {
                text = choice
                setOnClickListener {
                    handleChoiceSelected(choice)
                }
            }
            choiceLayout.addView(button)
        }
    }

    /**
     * 選択肢が選ばれたときの処理
     */
    private fun handleChoiceSelected(selected: String) {
        Toast.makeText(this, "選んだ文字：$selected", Toast.LENGTH_SHORT).show()

        // TODO: クイズ正解判定ロジックへ（次ステップ）
        exitQuizMode()
    }

    /**
     * クイズ解答モードを終了し、ゲームを再開する
     */
    private fun exitQuizMode() {
        choiceLayout.visibility = View.GONE
        gameView.isInQuizMode = false
        answerButton.isEnabled = true
        gameView.invalidate()
    }
}
