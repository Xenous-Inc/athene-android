package com.xenous.athenekotlin.views.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word

class CategoriesRecyclerViewAdapter(
    private val activity: Activity,
    private val categoriesList: List<Category>,
    private val wordsList: List<Word>
) : RecyclerView.Adapter<CategoriesRecyclerViewAdapter.CategoriesRecyclerViewHolder>() {

    private val notEmptyCategories = mutableListOf<Category>()
    private val wordInCategoriesMatrix = mutableListOf<List<Word>>()

    init {
        for(category in categoriesList) {
            if(category.title.isEmpty() || category.title == "Без категории") { //Replace with string resource
                continue
            }

            val wordInCurrentCategoryMutableList = mutableListOf<Word>()
            for(word in wordsList) {
                if(word.category == category.title) {
                    wordInCurrentCategoryMutableList.add(word)
                }
            }

            if(wordInCurrentCategoryMutableList.size != 0) {
                notEmptyCategories.add(category)
                wordInCategoriesMatrix.add(wordInCurrentCategoryMutableList.toList())
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoriesRecyclerViewHolder {
        return CategoriesRecyclerViewHolder(LayoutInflater.from(activity).inflate(R.layout.layout_cell_category, parent, false))
    }

    override fun getItemCount(): Int {
        return notEmptyCategories.size
    }

    override fun onBindViewHolder(holder: CategoriesRecyclerViewHolder, position: Int) {
        //Call views you need and do with their methods all what you wont
        //You can call them follow this example

        holder.categoryTitleTextView.text = notEmptyCategories[position].title
    }


    inner class CategoriesRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init here all views which you need to add logic
        val categoryCardView = itemView.findViewById<CardView>(R.id.categoryCardView)
        val categoryTitleTextView = itemView.findViewById<TextView>(R.id.categoryTitleTextView)

    }
}