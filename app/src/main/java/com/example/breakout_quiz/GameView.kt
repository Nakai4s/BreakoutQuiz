package com.example.breakout_quiz

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * ゲームの描画とロジックを担うカスタムView。
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface GameEventListener {
        fun onBallMissed()
    }

    var gameEventListener: GameEventListener? = null
    var isInQuizMode: Boolean = false

    // 描画に使用するPaint群
    private val paddlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.YELLOW }
    private val blockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        style = Paint.Style.FILL
    }
    private val questionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        textAlign = Paint.Align.LEFT
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 48f
        textAlign = Paint.Align.CENTER
    }

    // オブジェクトと状態
    private var ball = Ball(x = 0f, y = 0f, speedMultiplier = 1f)
    private var paddle = Paddle(x = 0f, y = 0f, width = 300f)
    private val blocks = mutableListOf<Block>()
    private var viewWidth = 0
    private var viewHeight = 0
    private var isGameRunning = false
    private var lastUpdateTime = System.currentTimeMillis()
    companion object{
        var isCountdownActive: Boolean = false
    }

    // ステージ設定
    private var backgroundColor: Int = Color.BLACK
    private var blockColor: Int = Color.CYAN
    private var currentQuestion: String = "question..."
    private var currentHint: String = "hint..."

    private val handler = Handler(Looper.getMainLooper())
    private val frameRate: Long = 16 // 約60FPS

    // ブロック最上段の高さ
    private var blockTopY: Float = 0f

    fun setQuestion(question: String, hint: String) {
        currentQuestion = question
        currentHint = hint
    }

    /**
     * ステージごとに背景やブロックの色を変更する
     */
    fun setStageColors(backgroundHex: String, blockHex: String) {
        backgroundColor = Color.parseColor(backgroundHex)
        blockColor = Color.parseColor(blockHex)
        invalidate()
    }

    fun startGame() {
        isGameRunning = true
        invalidate()
    }

    fun regenerateBlocks() {
        generateBlocks()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        paddle.x = w / 2f
        paddle.y = h * 0.9f
        // initBall()
        generateBlocks()
        resetBall()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)

        if (isGameRunning) {
            // 経過時間（デルタタイム）
            val now = System.currentTimeMillis()
            val deltaMillis = now - lastUpdateTime
            lastUpdateTime = now

            if (!isCountdownActive && !isInQuizMode) updateGame(deltaMillis)
            drawBackgroundHint(canvas)
            drawBlocks(canvas)
            drawBall(canvas)
            drawPaddle(canvas)
            drawForegroundQuestion(canvas)
            handler.postDelayed({ invalidate() }, frameRate)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isGameRunning) return true
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
            paddle.x = event.x.coerceIn(paddle.width / 2, viewWidth - paddle.width / 2)
            invalidate()
        }
        return true
    }

    private fun initBall() {
        //ball = Ball(x = viewWidth * 0.5f, y = viewHeight * 0.5f, )
        ball.speedMultiplier = ball.initSpeed
    }

    fun resetBall() {
        ball.x = viewWidth / 2f
        ball.y = viewHeight * 0.8f
        ball.dx = if (Math.random() < 0.5) -1f else 1f
        ball.dy = -1f
        ball.speedMultiplier = ball.initSpeed
    }

    /**
     * 主にボールの更新処理を行う。
     * @param deltaMillis デルタタイム
     */
    private fun updateGame(deltaMillis: Long) {
        updateBall(deltaMillis)
        checkBlockCollision()
    }

    private fun updateBall(deltaMillis: Long) {
        ball.speedMultiplier += ball.accelerationRate
        val deltaTimeSec = deltaMillis / 1000f
        ball.x += ball.dx * deltaTimeSec * ball.speedMultiplier
        ball.y += ball.dy * deltaTimeSec * ball.speedMultiplier

        // 壁反射
        if (ball.x - ball.radius < 0 || ball.x + ball.radius > viewWidth) {
            ball.dx = -ball.dx
            ball.x = ball.x.coerceIn(ball.radius, viewWidth - ball.radius)
        }
        if (ball.y - ball.radius < blockTopY) {
            ball.dy = -ball.dy
            ball.y = blockTopY + ball.radius
        }

        // パドルと衝突
        if (ball.y + ball.radius >= paddle.y && ball.x in paddle.left()..paddle.right()) {
            ball.dy = -Math.abs(ball.dy)
            val offset = (ball.x - paddle.x) / (paddle.width / 2)
            ball.dx += offset * 2f
        }

        // 落下チェック
        if (ball.y - ball.radius > viewHeight) {
            gameEventListener?.onBallMissed()
            stopGame()
        }
    }

    private fun stopGame() {
        isGameRunning = false
    }

    private fun drawPaddle(canvas: Canvas) {
        canvas.drawRect(paddle.left(), paddle.y, paddle.right(), paddle.y + paddle.height, paddlePaint)
    }

    private fun drawBall(canvas: Canvas) {
        canvas.drawCircle(ball.x, ball.y, ball.radius, ballPaint)
    }

    private fun drawBlocks(canvas: Canvas) {
        blockPaint.color = blockColor
        for (block in blocks) {
            if (block.isVisible) {
                canvas.drawRect(block.left, block.top, block.right, block.bottom, blockPaint)
            }
        }
    }

    private fun drawForegroundQuestion(canvas: Canvas) {
        val x = viewWidth * 0.05f
        val y = viewHeight * 0.1f
        canvas.drawText(currentQuestion, x, y, questionPaint)
    }

    private fun drawBackgroundHint(canvas: Canvas) {
        val x = viewWidth / 2f
        val y = viewHeight * 0.25f
        canvas.drawText(currentHint, x, y, hintPaint)
    }

    private fun checkBlockCollision() {
        for (block in blocks) {
            if (block.isVisible &&
                ball.x + ball.radius > block.left &&
                ball.x - ball.radius < block.right &&
                ball.y + ball.radius > block.top &&
                ball.y - ball.radius < block.bottom
            ) {
                block.isVisible = false
                ball.dy = -ball.dy
                break
            }
        }
    }

    private fun generateBlocks() {
        blocks.clear()
        val blockRows = 5
        val blockCols = 6
        val blockPadding = 8f
        val blockWidth = (viewWidth - (blockCols + 1) * blockPadding) / blockCols
        val blockHeight = viewHeight * 0.05f

        for (row in 0 until blockRows) {
            for (col in 0 until blockCols) {
                val left = blockPadding + col * (blockWidth + blockPadding)
                val top = blockPadding + row * (blockHeight + blockPadding)+ viewHeight * 0.12f
                val right = left + blockWidth
                val bottom = top + blockHeight
                blocks.add(Block(left, top, right, bottom))

                if (row == 0 && col == 0) {
                    blockTopY = top // 最上段のtop位置を記録
                }
            }
        }
    }
}
