package com.halfback.breakout_quiz

data class Ball(
    var x: Float,
    var y: Float,

    // 半径
    val radius: Float = 16f,
    // x方向(-1 <= x <= 1)
    var dx: Float = 1f,
    // y方向(-1 <= y <= 1)
    var dy: Float = -1f,
    // 速度
    val speedMultiplier: Float = 500.0f,
)