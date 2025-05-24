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
import android.widget.TableRow
import com.example.breakout_quiz.utils.SoundManager

/**
 * ゲームの描画とロジックを担うカスタムView。
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface GameEventListener {
        fun onBallMissed()
        fun onPaddleHit()
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
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.LEFT
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
        currentQuestion = "Q:$question"
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
        paddle.x = w / 2f
        paddle.y = h * 0.75f
        generateBlocks(5, 6)
        resetBall()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(backgroundColor)

        // 経過時間（デルタタイム）
        val now = System.currentTimeMillis()
        val deltaMillis = now - lastUpdateTime
        lastUpdateTime = now

        if (isGameRunning) {
            // ゲーム動作中のみ更新処理を行う
            if (!isCountdownActive && !isInQuizMode) updateGame(deltaMillis)
        }

        // 描画系は常に表示（カウントダウン中はUIが覆うので問題ない）
        drawBackgroundHint(canvas)
        drawBlocks(canvas)
        drawBall(canvas)
        drawPaddle(canvas)
        drawForegroundQuestion(canvas)
        handler.postDelayed({ invalidate() }, frameRate)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isGameRunning) return true
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
            paddle.x = event.x.coerceIn(paddle.width / 2, viewWidth - paddle.width / 2)
            invalidate()
        }
        return true
    }

    fun resetBall() {
        if (viewWidth > 0 && viewHeight > 0) {
            ball.x = viewWidth / 2f
            ball.y = viewHeight * 0.6f
        }
        ball.dx = if (Math.random() < 0.5) -1f else 1f
        ball.dy = -1f
        ball.speedMultiplier = ball.initSpeed
    }

    /**
     * ボールとブロックの更新処理を行う。
     * @param deltaMillis デルタタイム
     */
    private fun updateGame(deltaMillis: Long) {
        updateBall(deltaMillis)
        //checkBlockCollision()
    }

    /**
     * ボールの更新処理を行う
     * @param deltaMillis デルタタイム
     */
    private fun updateBall(deltaMillis: Long) {
        // 移動量を計算（方向は実際に座標を変更する際に考慮する）
        val totalMoveX = (deltaMillis / 1000f) * ball.speedMultiplier
        val totalMoveY = (deltaMillis / 1000f) * ball.speedMultiplier

        // ステップ数を動きが多い時ほど増やす（最大16分割などで十分）
        val step = 8
        val stepX = totalMoveX / step
        val stepY = totalMoveY / step

        for (i in 1..step) {
            ball.x += stepX * ball.dx
            ball.y += stepY * ball.dy

            if (checkBlockCollision()) break // ← これで反応が漏れにくくなる

            // 壁反射
            if (ball.x - ball.radius < 0f) {
                ball.x = ball.radius + 0.01f
                ball.dx = -ball.dx
                continue
            }
            if (ball.x + ball.radius > viewWidth) {
                ball.x = viewWidth - ball.radius + 0.01f
                ball.dx = -ball.dx
                continue
            }

            // 上端反射（ブロックのtop）
            if (ball.y - ball.radius < blockTopY) {
                ball.y = blockTopY + ball.radius
                ball.dy = -ball.dy
                continue
            }

            // パドル判定
            if (ball.y + ball.radius >= paddle.y && ball.y - ball.radius < paddle.y + paddle.height &&
                ball.x in paddle.left()..paddle.right()
            ) {
                ball.y = paddle.y - ball.radius // パドル上面で止める
                ball.dy = -Math.abs(ball.dy)
                val offset = (ball.x - paddle.x) / (paddle.width / 2)
                ball.dx += offset * 2f

                gameEventListener?.onPaddleHit()
                SoundManager.play("paddle")
                continue
            }

            // 落下
            if (ball.y - ball.radius > viewHeight) {
                gameEventListener?.onBallMissed()
                stopGame()
                return
            }
        }
    }

    fun stopGame() {
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
        val x = viewWidth * 0.05f
        val lines = currentHint.split("\n")
        val baseY = viewHeight * 0.15f
        // ブロックの高さを基準に計算
        val lineSpacing = viewHeight * 0.05f
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, x, baseY + index * lineSpacing, hintPaint)
        }
    }

    /**
     * ブロックとの衝突判定を行う。
     */
    private fun checkBlockCollision(): Boolean {
        for (block in blocks) {
            if (!block.isVisible) continue

            // 衝突チェック（AABB）
            val collision = ball.x + ball.radius > block.left &&
                    ball.x - ball.radius < block.right &&
                    ball.y + ball.radius > block.top &&
                    ball.y - ball.radius < block.bottom

            if (collision) {
                block.isVisible = false

                // 反射方向を精密判定（既に導入済みと仮定）
                val overlapLeft = ball.x + ball.radius - block.left
                val overlapRight = block.right - (ball.x - ball.radius)
                val overlapTop = ball.y + ball.radius - block.top
                val overlapBottom = block.bottom - (ball.y - ball.radius)

                val minOverlapX = minOf(overlapLeft, overlapRight)
                val minOverlapY = minOf(overlapTop, overlapBottom)

                if (minOverlapX < minOverlapY) {
                    ball.dx = -ball.dx
                } else {
                    ball.dy = -ball.dy
                }

                return true // 衝突あり
            }
        }
        return false // 衝突なし
    }


    /**
     * ブロックを生成する
     * @param rows 行数
     * @param columns 列数
     * @param padding パディング（デフォルトで8f）
     */
    fun generateBlocks(rows: Int, columns: Int, padding: Float = 8f) {
        blocks.clear()
        val blockWidth = (viewWidth - (columns + 1) * padding) / columns
        val blockHeight = viewHeight * 0.05f

        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val left = padding + col * (blockWidth + padding)
                val top = padding + row * (blockHeight + padding)+ viewHeight * 0.12f
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
