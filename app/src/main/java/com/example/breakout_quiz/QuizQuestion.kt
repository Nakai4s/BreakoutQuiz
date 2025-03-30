package com.example.breakout_quiz

/**
 * クイズの1問分のデータを表すクラス。
 */
data class QuizQuestion(
    val question: String,     // 文字列①：問題文
    val hint: String,         // 文字列②：ヒント文
    val answer: List<String>,
    val choices: List<List<String>>,
//    val backgroundColor: String = "#000000", // default black
//    val blockColor: String = "#00FFFF"       // default cyan
)