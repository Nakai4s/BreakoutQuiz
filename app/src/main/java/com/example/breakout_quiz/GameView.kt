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

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // パドル
    private val paddlePaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }

    private var paddle = Paddle(
        x = 0f,
        y = 0f,
        width = 300f
    )

    // ボール
    private val ballPaint = Paint().apply {
        color = Color.YELLOW
        isAntiAlias = true
    }

    /* ブロック start */
    private val blockPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.FILL
    }
    private val blocks = mutableListOf<Block>()
    private val blockRows = 5
    private val blockCols = 6
    private val blockPadding = 8f
    /* ブロック end */


    private lateinit var ball: Ball
    private val frameRate: Long = 16 // 60FPS 相当
    private val handler = Handler(Looper.getMainLooper())

    private fun initBall() {
        ball = Ball(
            x = viewWidth / 2f,
            y = viewHeight * 0.5f
        )
    }

    private var viewWidth = 0
    private var viewHeight = 0

    private var isGameRunning = false

    fun startGame() {
        isGameRunning = true
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        paddle.x = w / 2f
        paddle.y = h * 0.9f
        initBall()
        generateBlocks()
    }

    private fun stopGame() {
        isGameRunning = false
    }

    /**
     * ゲーム画面の描画を行う。
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isGameRunning) {
            updateBall()
            checkBlockCollision()
            drawBlocks(canvas)
            drawBall(canvas)
            drawPaddle(canvas)

            handler.postDelayed({ invalidate() }, frameRate)
        }
    }

    private fun drawPaddle(canvas: Canvas) {
        canvas.drawRect(
            paddle.left(),
            paddle.y,
            paddle.right(),
            paddle.y + paddle.height,
            paddlePaint
        )
    }

    private fun updateBall() {
        // 速度に加速
        ball.speedMultiplier += ball.accelerationRate

        ball.x += ball.dx * ball.speedMultiplier
        ball.y += ball.dy * ball.speedMultiplier

        // 左右の壁で反射
        if (ball.x - ball.radius < 0 || ball.x + ball.radius > viewWidth) {
            ball.dx = -ball.dx
            ball.x = ball.x.coerceIn(ball.radius, viewWidth - ball.radius)
        }

        // 上端で反射
        if (ball.y - ball.radius < 0) {
            ball.dy = -ball.dy
            ball.y = ball.radius
        }

        // パドルと衝突
        if (ball.y + ball.radius >= paddle.y &&
            ball.x in paddle.left()..paddle.right()
        ) {
            ball.dy = -Math.abs(ball.dy) // 上向きに反射

            // パドルの中心からの距離で角度調整
            val offset = (ball.x - paddle.x) / (paddle.width / 2)
            ball.dx += offset * 2f
        }

        // 画面下に落下 → リトライ or 終了（次ステップで実装）
        if (ball.y - ball.radius > viewHeight) {
            stopGame()
            // TODO: リトライ回数チェック & リザルト画面へ
        }
    }


    private fun drawBall(canvas: Canvas) {
        canvas.drawCircle(ball.x, ball.y, ball.radius, ballPaint)
    }

    /**
     * ブロックを画面上部に格子状に生成します。
     */
    private fun generateBlocks() {
        blocks.clear()
        val blockWidth = (viewWidth - (blockCols + 1) * blockPadding) / blockCols
        val blockHeight = viewHeight * 0.05f

        for (row in 0 until blockRows) {
            for (col in 0 until blockCols) {
                val left = blockPadding + col * (blockWidth + blockPadding)
                val top = blockPadding + row * (blockHeight + blockPadding)
                val right = left + blockWidth
                val bottom = top + blockHeight
                blocks.add(Block(left, top, right, bottom))
            }
        }
    }

    /**
     * 現在表示状態のブロックをすべて描画します。
     */
    private fun drawBlocks(canvas: Canvas) {
        for (block in blocks) {
            if (block.isVisible) {
                canvas.drawRect(block.left, block.top, block.right, block.bottom, blockPaint)
            }
        }
    }

    /**
     * ボールがブロックに当たったかを判定し、削除・反射処理を行います。
     */
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



    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isGameRunning) return true

        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                paddle.x = event.x.coerceIn(paddle.width / 2, viewWidth - paddle.width / 2)
                invalidate()
            }
        }
        return true
    }
}
