package com.halfback.breakout_quiz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.halfback.breakout_quiz.utils.WindowInsetsUtil

/**
 * クイズジャンル選択画面のアクティビティ
 */
class GenreSelectActivity : AppCompatActivity() {

    // jsonファイル名と同じにする
    private val genreList = listOf(
        "1"
        //"スポーツ", "歴史","国語", "理科","社会","芸能","アニメ・漫画","一般常識"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genre_select)

        WindowInsetsUtil.applySafePadding(findViewById(R.id.rootLayout))

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        // Contextが有効な状態で呼び出す
        val scores = loadGenreScores()

        // 2列で表示する
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = GenreAdapter(genreList, scores) { selectedGenre ->
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("genre", selectedGenre)
            startActivity(intent)
        }
    }

    /**
     * ジャンルごとのハイスコアを読み込む
     */
    private fun loadGenreScores(): Map<String, Int> {
        val prefs = getSharedPreferences("highscores", MODE_PRIVATE)
        return genreList.associateWith { genre ->
            prefs.getInt("highscore_$genre", 0)
        }
    }
}
