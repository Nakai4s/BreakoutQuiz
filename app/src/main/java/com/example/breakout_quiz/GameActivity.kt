package com.example.breakout_quiz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.breakout_quiz.utils.WindowInsetsUtil

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var countdownOverlay: CountdownOverlay
    private lateinit var answerButton: Button
    private lateinit var choiceLayout: LinearLayout
    private lateinit var feedbackText: TextView

    // クイズマネージャー
    private val quizManager = QuizManager()

    // リトライ可能数
    private var retryCount = 2

    // ゲームタイマー
    companion object {
        private const val TOTAL_TIME_MS = 60_000L  // 制限時間（1分）
    }
    private lateinit var timerText: TextView
    private var remainingTimeMs = TOTAL_TIME_MS
    private var gameTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val rootView = findViewById<View>(R.id.rootLayout) // ルートレイアウトのID
        WindowInsetsUtil.applySafePadding(rootView)

        timerText = findViewById(R.id.timer_text)
        feedbackText = findViewById(R.id.feedback_text)

        quizManager.loadQuestionsFromAssets(this) // ← JSON読み込み

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

        // ゲーム更新前の初期値をセットする
        init()

        startCountdown()

        gameView.gameEventListener = object : GameView.GameEventListener {
            override fun onBallMissed() {
                retryCount--
                if (retryCount >= 0) {
                    Toast.makeText(this@GameActivity, "リトライ残り：$retryCount", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        gameView.resetBall()
                        gameView.startGame()
                    }, 1000)
                } else {
                    goToResultScreen()
                }
            }
        }
    }

    /**
     * 初期化する
     */
    private fun init() {
        var q = quizManager.getCurrentQuestion()
        gameView.setQuestion(q.question)
    }

    private fun startCountdown() {
        countdownOverlay.startCountdown {
            remainingTimeMs = TOTAL_TIME_MS // ← これを必ず最初に入れる！
            startTimer()
            gameView.startGame()
        }
    }

    private fun startTimer() {
        gameTimer?.cancel() // 再起動用に一度キャンセル

        gameTimer = object : CountDownTimer(remainingTimeMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMs = millisUntilFinished
                val secondsLeft = millisUntilFinished / 1000
                timerText.text = "残り：${secondsLeft}秒"
            }

            override fun onFinish() {
                goToResultScreen()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameTimer?.cancel()
    }

    /**
     * クイズ解答モードに切り替え、選択肢を表示する
     */
    private fun enterQuizMode() {
        gameTimer?.cancel() // タイマー一時停止

        gameView.isInQuizMode = true
        choiceLayout.visibility = View.VISIBLE
        answerButton.isEnabled = false

        showChoices(quizManager.getCurrentQuestion().choices[quizManager.getCurrentStep()])
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
        when (val result = quizManager.submitAnswer(selected)) {
            null -> {
                // 正解（継続中）
                showChoices(quizManager.getCurrentQuestion().choices[quizManager.getCurrentStep()])
            }
            true -> {
                showFeedback("正解！", true)
                quizManager.moveToNextQuestion()
                Handler(Looper.getMainLooper()).postDelayed({
                    resetForNextQuestion()
                }, 800)
            }
            false -> {
                showFeedback("不正解", false)
                quizManager.moveToNextQuestion()
                Handler(Looper.getMainLooper()).postDelayed({
                    exitQuizMode()
                }, 800)
            }
        }
    }

    /**
     * 正解・不正解時のフィードバックを表示する
     */
    private fun showFeedback(message: String, isCorrect: Boolean) {
        feedbackText.text = message
        feedbackText.setTextColor(if (isCorrect) 0xFF00FF00.toInt() else 0xFFFF4444.toInt()) // 緑 or 赤
        feedbackText.visibility = View.VISIBLE

        feedbackText.alpha = 0f
        feedbackText.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction {
                feedbackText.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .setStartDelay(400)
                    .start()
            }
            .start()
    }


    private fun resetForNextQuestion() {
        val q = quizManager.getCurrentQuestion()
        gameView.setStageColors(q.backgroundColor, q.blockColor)
        // 問題文をセットする
        gameView.setQuestion(q.question)

        choiceLayout.visibility = View.GONE
        gameView.isInQuizMode = false
        answerButton.isEnabled = true

        // 新しい問題＆ブロック再生成
        gameView.regenerateBlocks() // GameView で public にする
        gameView.invalidate()
        startTimer() // タイマー再開（残り時間で）
    }


    /**
     * クイズ解答モードを終了し、ゲームを再開する
     */
    private fun exitQuizMode() {

        choiceLayout.visibility = View.GONE
        gameView.isInQuizMode = false
        answerButton.isEnabled = true

        gameView.invalidate()
        startTimer() // タイマー再開（残り時間で）
    }

    /**
     * リザルト画面へ遷移する
     */
    private fun goToResultScreen() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("score", quizManager.getScore())
        startActivity(intent)
        finish()
    }
}
