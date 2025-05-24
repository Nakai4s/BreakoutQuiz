package com.halfback.breakout_quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GenreAdapter(
    private val genres: List<String>,
    private val scores: Map<String, Int>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    inner class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val genreText: TextView = itemView.findViewById(R.id.genre_name)
        val scoreText: TextView = itemView.findViewById(R.id.genre_score)

        init {
            itemView.setOnClickListener {
                //onClick(genres[adapterPosition])
                onClick(genres[absoluteAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        val genre = genres[position]
        holder.genreText.text = genre
        holder.scoreText.text = "最高正答数：${scores[genre] ?: 0}問"
    }

    override fun getItemCount(): Int = genres.size
}
