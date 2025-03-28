package com.example.breakout_quiz

data class Ball(
    var x: Float,
    var y: Float,
    var radius: Float = 16f,
    var dx: Float = 5f,
    var dy: Float = -5f,
    var speedMultiplier: Float = 1.0f,
    val accelerationRate: Float = 0.002f // 毎フレーム速度が増える量
)
