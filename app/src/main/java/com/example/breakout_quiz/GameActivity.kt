package com.example.breakout_quiz

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.breakout_quiz.utils.SoundManager
import com.example.breakout_quiz.utils.WindowInsetsUtil
import org.w3c.dom.Text

/**
 * ゲーム画面のアクティビティ。ブロック崩しとクイズを同時に制御します。
 */
class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var countdownOverlay: CountdownOverlay
    private lateinit var answerButton: Button
    private lateinit var choiceLayout: LinearLayout

    private lateinit var lifeText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var trueCount: TextView

    private val quizManager = QuizManager()
    private var retryCount = 2

    // プログレスバー
    private lateinit var gameTimerBar: ProgressBar
    private var remainingTimeMs = TOTAL_TIME_MS
    private var gameTimer: CountDownTimer? = null

    // 選択肢関連
    private var choiceTimeoutHandler = Handler(Looper.getMainLooper())
    private var choiceTimeoutRunnable: Runnable? = null
    private val choiceTimeoutMs = 5000L

    // クイズジャンル（リザルト画面に引き継ぐ）
    private lateinit var genre: String

    /**
     * ゲーム時間を設定
     */
    companion object {
        private const val TOTAL_TIME_MS = 60_000L // 1分
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SoundManager.initialize(this)

        setContentView(R.layout.activity_game)

        WindowInsetsUtil.applySafePadding(findViewById(R.id.rootLayout))

        gameView = findViewById(R.id.game_view)
        countdownOverlay = findViewById(R.id.countdown_overlay)
        answerButton = findViewById(R.id.answer_button)
        choiceLayout = findViewById(R.id.choice_layout)

        lifeText = findViewById(R.id.life_text)
        updateLifeDisplay()
        feedbackText = findViewById(R.id.feedback_text)
        trueCount = findViewById(R.id.true_count)
        gameTimerBar = findViewById(R.id.game_timer_bar)

        // ジャンルごとのクイズデータを設定
        genre = intent.getStringExtra("genre") ?: "default"
        val filename = "quizdata_${genre}.json"
        quizManager.loadQuestionsFromAssets(this, filename)

        answerButton.setOnClickListener { enterQuizMode() }

        gameView.gameEventListener = object : GameView.GameEventListener {
            /**
             * ボールが落下した場合
             */
            override fun onBallMissed() {
                retryCount--
                updateLifeDisplay()
                if (retryCount >= 0) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        gameView.resetBall()
                        gameView.startGame()
                    }, 1000)
                } else {
                    gameView.stopGame()
                    Handler(Looper.getMainLooper()).postDelayed({
                        goToResultScreen()
                    }, 2000)
                }
            }

            /**
             * パドルがボールに接触した場合
             */
            override fun onPaddleHit() {
                SoundManager.play("paddle")
            }
        }

        startCountdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameTimer?.cancel()
        SoundManager.release()
    }

    /**
     * 残基を更新する
     */
    private fun updateLifeDisplay() {
        val lifeSymbol = "●"
        val display = lifeSymbol.repeat(retryCount + 1)
        lifeText.text = display
    }

    /**
     * カウントダウンを開始する
     */
    private fun startCountdown() {
        countdownOverlay.startCountdown {
            // カウントダウン終了時にボールを中央リセット
            gameView.resetBall()

            remainingTimeMs = TOTAL_TIME_MS
            startTimer()
            startNewQuestion()
            gameView.startGame()

            // カウントダウン終了後にボタンを表示
            answerButton.visibility = View.VISIBLE
        }
    }

    private fun startTimer() {
        gameTimer?.cancel()
        gameTimer = object : CountDownTimer(remainingTimeMs, 100) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMs = millisUntilFinished
                val progress = (remainingTimeMs * 100 / TOTAL_TIME_MS).toInt()
                gameTimerBar.progress = progress
            }

            override fun onFinish() {
                gameView.stopGame()
                Handler(Looper.getMainLooper()).postDelayed({
                    goToResultScreen()
                }, 2000)
            }
        }.start()
    }

    /**
     * クイズ解答モードに入る
     */
    private fun enterQuizMode() {
        gameTimer?.cancel()
        gameView.isInQuizMode = true
        choiceLayout.visibility = View.VISIBLE
        answerButton.isEnabled = false

        showChoices(quizManager.getCurrentQuestion().choices[quizManager.getCurrentStep()])
        // タイムアウトの計測を開始する
        startChoiceTimeout()
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

    /**
     * 選択肢を連続で押下するのを防止する
     */
    private fun disableAllChoiceButtons() {
        for (i in 0 until choiceLayout.childCount) {
            val child = choiceLayout.getChildAt(i)
            if (child is Button) {
                child.isEnabled = false
            }
        }
    }

    /**
     * 選択肢を押下した際の挙動を制御する
     */
    private fun handleChoiceSelected(selected: String) {
        // すべての選択肢を選択不可にする
        disableAllChoiceButtons()

        // 一旦タイマー停止
        choiceTimeoutRunnable?.let { choiceTimeoutHandler.removeCallbacks(it) }

        when (val result = quizManager.submitAnswer(selected)) {
            null -> {
                showChoices(quizManager.getCurrentQuestion().choices[quizManager.getCurrentStep()])
                // 次の文字選択タイマー開始
                startChoiceTimeout()
            }
            true -> {
                SoundManager.play("true")
                showFeedback("正解！", true)
                trueCount.text = "正答数：${quizManager.getScore()}"
                quizManager.moveToNextQuestion(true)
                Handler(Looper.getMainLooper()).postDelayed({ startNewQuestion() }, 800)
            }
            false -> {
                SoundManager.play("false")
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
        gameView.generateBlocks(5, 6)
        gameView.resetBall()
        exitQuizMode()
    }

    private fun exitQuizMode() {
        startTimer()

        // タイムアウト監視用のタイマー停止
        choiceTimeoutRunnable?.let { choiceTimeoutHandler.removeCallbacks(it) }
        choiceLayout.visibility = View.GONE
        gameView.isInQuizMode = false
        answerButton.isEnabled = true
        gameView.invalidate()
    }

    /**
     * 解答の正否を表示する
     * @param message 表示する文字列
     * @param isCorrect 正解・不正解
     */
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

    /**
     * タイムアウトを監視する
     */
    private fun startChoiceTimeout() {
        // 既存タイマー停止
        choiceTimeoutRunnable?.let { choiceTimeoutHandler.removeCallbacks(it) }

        choiceTimeoutRunnable = Runnable {
            SoundManager.play("false")
            showFeedback("不正解", false)
            quizManager.moveToNextQuestion(false)
            Handler(Looper.getMainLooper()).postDelayed({ exitQuizMode() }, 800)
        }
        choiceTimeoutHandler.postDelayed(choiceTimeoutRunnable!!, choiceTimeoutMs)
    }


    /**
     * プログレスバーのアニメーションを調整する
     * @param bar プログレスバーのオブジェクト
     * @param to
     * @param duration
     */
    fun animateProgressBar(bar: ProgressBar, to: Int, duration: Long = 1000) {
        var animation = ObjectAnimator.ofInt(bar, "progress", bar.progress)
        animation.duration = duration
        bar.setProgress(bar.progress, true)
    }


    /**
     * リザルト画面へ遷移する
     */
    private fun goToResultScreen() {
        val currentScore = quizManager.getScore()
        val prefs = getSharedPreferences("highscores", MODE_PRIVATE)
        val key = "highscore_$genre"
        val best = prefs.getInt(key, 0)

        // 上書き保存（スコアが更新された場合）
        if (currentScore > best) {
            prefs.edit().putInt(key, currentScore).apply()
        }

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("score", currentScore)
        intent.putExtra("genre", genre)
        startActivity(intent)
        finish()
    }
}
