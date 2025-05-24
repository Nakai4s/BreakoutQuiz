package com.halfback.breakout_quiz

/**
 * ゲーム画面上に表示される1つのブロックを表すデータクラス。
 */
data class Block(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    var isVisible: Boolean = true
)
