package com.example.breakout_quiz

data class Ball(
    var x: Float,
    var y: Float,
    var radius: Float = 16f,
    var dx: Float = 1f,
    var dy: Float = -1f,
    val initSpeed: Float = 200.0f,
    var speedMultiplier: Float,
    val accelerationRate: Float = 1f // 毎フレーム速度が増える量
)
