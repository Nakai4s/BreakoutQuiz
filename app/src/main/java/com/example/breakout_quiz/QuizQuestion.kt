package com.example.breakout_quiz

/**
 * クイズの1問分のデータを表すクラス。
 */
data class QuizQuestion(
    val question: String,
    val answer: List<String>,
    val choices: List<List<String>>,
    val backgroundColor: String = "#000000", // default black
    val blockColor: String = "#00FFFF"       // default cyan
)