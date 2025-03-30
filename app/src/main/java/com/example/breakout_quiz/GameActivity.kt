package com.example.breakout_quiz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.breakout_quiz.utils.WindowInsetsUtil

/**
 * ゲーム画面のアクティビティ。ブロック崩しとクイズを同時に制御します。
 */
class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var countdownOverlay: CountdownOverlay
    private lateinit var answerButton: Button
    private lateinit var choiceLayout: LinearLayout
    private lateinit var timerText: TextView
    private lateinit var feedbackText: TextView

    private val quizManager = QuizManager()
    private var retryCount = 1
    private var remainingTimeMs = TOTAL_TIME_MS
    private var gameTimer: CountDownTimer? = null

    companion object {
        private const val TOTAL_TIME_MS = 60_000L // 1分
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        WindowInsetsUtil.applySafePadding(findViewById(R.id.rootLayout))

        gameView = findViewById(R.id.game_view)
        countdownOverlay = findViewById(R.id.countdown_overlay)
        answerButton = findViewById(R.id.answer_button)
        choiceLayout = findViewById(R.id.choice_layout)
        timerText = findViewById(R.id.timer_text)
        feedbackText = findViewById(R.id.feedback_text)

        quizManager.loadQuestionsFromAssets(this)

        answerButton.setOnClickListener { enterQuizMode() }

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

        startCountdown()
    }

    private fun startCountdown() {
        countdownOverlay.startCountdown {
            remainingTimeMs = TOTAL_TIME_MS
            startTimer()
            startNewQuestion()
            gameView.startGame()
        }
    }

    private fun startTimer() {
        gameTimer?.cancel()
        gameTimer = object : CountDownTimer(remainingTimeMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMs = millisUntilFinished
                timerText.text = "残り：${millisUntilFinished / 1000}秒"
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

    private fun enterQuizMode() {
        gameTimer?.cancel()
        gameView.isInQuizMode = true
        choiceLayout.visibility = View.VISIBLE
        answerButton.isEnabled = false

        showChoices(quizManager.getCurrentQuestion().choices[quizManager.getCurrentStep()])
    }

    private fun showChoices(choices: List<String>) {
        choiceLayout.removeAllViews()
        choices.forEach { choice ->
            Button(this).apply {
                text = choice
                setOnClickListener { handleChoiceSelected(choice) }
                choiceLayout.addView(this)
            }
        }
    }

    private fun handleChoiceSelected(selected: String) {
        when (val result = quizManager.submitAnswer(selected)) {
            null -> showChoices(quizManager.getCurrentQuestion().choices[quizManager.getCurrentStep()])
            true -> {
                showFeedback("正解！", true)
                quizManager.moveToNextQuestion(true)
                Handler(Looper.getMainLooper()).postDelayed({ startNewQuestion() }, 800)
            }
            false -> {
                showFeedback("不正解", false)
                quizManager.moveToNextQuestion(false)
                Handler(Looper.getMainLooper()).postDelayed({ exitQuizMode() }, 800)
            }
        }
    }

    private fun startNewQuestion() {
        val question = quizManager.getCurrentQuestion()
        gameView.setQuestion(question.question, question.hint)
        // gameView.setStageColors(question.backgroundColor, question.blockColor)
        // gameView.regenerateBlocks()
        gameView.resetBall()
        exitQuizMode()
    }

    private fun exitQuizMode() {
        startTimer()
        choiceLayout.visibility = View.GONE
        gameView.isInQuizMode = false
        answerButton.isEnabled = true
        gameView.invalidate()
    }

    private fun showFeedback(message: String, isCorrect: Boolean) {
        feedbackText.text = message
        feedbackText.setTextColor(if (isCorrect) 0xFF00FF00.toInt() else 0xFFFF4444.toInt())
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

    private fun goToResultScreen() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("score", quizManager.getScore())
        startActivity(intent)
        finish()
    }
}
