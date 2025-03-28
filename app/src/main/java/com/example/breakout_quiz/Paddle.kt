package com.example.breakout_quiz

data class Paddle(
    var x: Float,             // 中心X座標
    var y: Float,             // 固定Y座標
    var width: Float,         // パドルの幅
    val height: Float = 20f   // パドルの高さ（固定でもOK）
)
{
    fun left() = x - width / 2
    fun right() = x + width / 2
}
