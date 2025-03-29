package com.example.breakout_quiz

/**
 * クイズの現在の問題と進行状態を管理するクラス。
 */
class QuizManager {

    data class QuizQuestion(
        val question: String,
        val answer: List<String>,
        val choices: List<List<String>>
    )

    private var currentIndex = 0
    private var currentStep = 0
    private var score = 0

    // 仮の問題リスト（後でJSONから読み込み）
    private val questions = listOf(
        QuizQuestion(
            question = "らいおん",
            answer = listOf("ら", "い", "お", "ん"),
            choices = listOf(
                listOf("ら", "あ", "う", "え"),
                listOf("い", "あ", "え", "お"),
                listOf("お", "え", "い", "う"),
                listOf("ん", "い", "あ", "え")
            )
        ),
        QuizQuestion(
            question = "すいか",
            answer = listOf("す", "い", "か"),
            choices = listOf(
                listOf("す", "あ", "い", "う"),
                listOf("い", "え", "お", "か"),
                listOf("か", "き", "く", "け")
            )
        )
    )

    fun getCurrentQuestion(): QuizQuestion = questions[currentIndex]
    fun getCurrentStep(): Int = currentStep
    fun getScore(): Int = score

    /**
     * 解答文字をチェックし、正解なら次のステップへ、不正解なら失敗として扱う。
     * @return 正解ならnull / 不正解ならfalse / 最後まで正解ならtrue
     */
    fun submitAnswer(choice: String): Boolean? {
        val current = getCurrentQuestion()

        return if (choice == current.answer[currentStep]) {
            currentStep++
            if (currentStep >= current.answer.size) {
                // 全問正解
                score++
                true
            } else {
                null // 継続中
            }
        } else {
            false // 不正解
        }
    }

    /**
     * 次の問題へ進む
     */
    fun moveToNextQuestion() {
        currentIndex = (currentIndex + 1) % questions.size
        currentStep = 0
    }
}
