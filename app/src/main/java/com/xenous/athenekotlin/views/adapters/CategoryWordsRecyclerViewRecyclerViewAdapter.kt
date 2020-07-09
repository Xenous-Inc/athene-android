package com.xenous.athenekotlin.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Word

class CategoryWordsRecyclerViewRecyclerViewAdapter(
    private val context: Context,
    private val wordsArrayList: ArrayList<Word>
) : RecyclerView.Adapter<CategoryWordsRecyclerViewRecyclerViewAdapter.CategoryWordsRecyclerViewRecyclerViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryWordsRecyclerViewRecyclerViewHolder {
        return CategoryWordsRecyclerViewRecyclerViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_cell_archived_word, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: CategoryWordsRecyclerViewRecyclerViewHolder,
        position: Int
    ) {

    }

    override fun getItemCount(): Int {
        return wordsArrayList.size
    }

    inner class CategoryWordsRecyclerViewRecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
    }
}