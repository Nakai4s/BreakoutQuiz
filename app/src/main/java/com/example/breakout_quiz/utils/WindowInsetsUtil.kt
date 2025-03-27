package com.example.breakout_quiz.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

object WindowInsetsUtil {

    fun applySafePadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                //left = systemBars.left,
                top = systemBars.top,
                //right = systemBars.right,
                //bottom = systemBars.bottom
            )

            insets
        }

        // リスナー設定後に再適用させる
        // ViewCompat.requestApplyInsets(view)
    }
}
