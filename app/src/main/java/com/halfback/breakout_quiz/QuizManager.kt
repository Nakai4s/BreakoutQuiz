package com.halfback.breakout_quiz

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * クイズの現在の問題と進行状態を管理するクラス。
 */
class QuizManager {

    private var currentStep = 0
    private var score = 0

    private var allQuestions: List<QuizQuestion> = emptyList()
    private var shuffledQuestions: MutableList<QuizQuestion> = mutableListOf()
    private var currentQuestionIndex = 0

    /**
     * アセットからJSONファイルを読み込んで問題リストを初期化します。
     */
    fun loadQuestionsFromAssets(context: Context, fileName: String = "quiz_data.json") {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<QuizQuestion>>() {}.type
        allQuestions = Gson().fromJson(jsonString, listType)

        shuffledQuestions = allQuestions.shuffled().toMutableList()
        currentQuestionIndex = 0
        currentStep = 0
        score = 0
    }

    fun getCurrentQuestion(): QuizQuestion = shuffledQuestions[currentQuestionIndex]
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
    fun moveToNextQuestion(isCorrect: Boolean) {
        if(isCorrect){
            currentQuestionIndex = (currentQuestionIndex + 1) % shuffledQuestions.size
        }
        currentStep = 0
    }

    /**
    * 残り問題数が0になったか判定します（未使用の場合は削除可）
    */
    fun isAllQuestionsAnswered(): Boolean {
        return currentQuestionIndex >= shuffledQuestions.size - 1
    }
}
