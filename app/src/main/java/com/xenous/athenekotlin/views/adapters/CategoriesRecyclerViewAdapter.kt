package com.xenous.athenekotlin.views.adapters

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.CategoryActivity
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.views.CategoryCellOpening
import java.util.*

class CategoriesRecyclerViewAdapter(
    private val activity: Activity,
    private val categoriesList: List<Category>,
    private val wordsList: List<Word>
) : RecyclerView.Adapter<CategoriesRecyclerViewAdapter.CategoriesRecyclerViewHolder>() {

    private val notEmptyCategories = mutableListOf<Category>()
    private val wordInCategoriesMatrix = mutableListOf<List<Word>>()
    private val categoryCellOpeningMutableList = mutableListOf<CategoryCellOpening>()

    init {
        for(category in categoriesList) {
            if(category.title.isEmpty() || category.title == "Без категории") { //Replace with string resource
                continue
            }

            val wordInCurrentCategoryMutableList = mutableListOf<Word>()
            for(word in wordsList) {
                if(
                    word.category.trim().toLowerCase(Locale.ROOT) ==
                    category.title.trim().toLowerCase(Locale.ROOT)
                ) {
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
        val categoryCellOpening =
            CategoryCellOpening.Builder().build(activity.layoutInflater)
        categoryCellOpeningMutableList.add(categoryCellOpening)

        return CategoriesRecyclerViewHolder(categoryCellOpening)
    }

    override fun getItemCount(): Int {
        return notEmptyCategories.size
    }

    override fun onBindViewHolder(holder: CategoriesRecyclerViewHolder, position: Int) {
        holder.categoryCardView.setOnClickListener {_ ->
            holder.categoryCellOpening.expand()

            categoryCellOpeningMutableList.forEach {
                if(it != holder.categoryCellOpening) { it.collapse() }
            }
        }
        holder.categoryTitleTextView.text = notEmptyCategories[position].title

        holder.actionAddToLearningLinearLayout.setOnClickListener {

        }
        holder.actionShareLinearLayout.setOnClickListener {  }
        holder.actionMoreDetailsLinearLayout.setOnClickListener {
            val intent = Intent(activity, CategoryActivity::class.java)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                holder.categoryTitleTextView,
                activity.getString(R.string.category_title_transition_name)
            )
            activity.startActivity(intent, options.toBundle())
        }
        holder.actionDeleteLinearLayout.setOnClickListener {  }
//        todo: add other methods
    }


    inner class CategoriesRecyclerViewHolder(val categoryCellOpening: CategoryCellOpening) : RecyclerView.ViewHolder(categoryCellOpening.view) {
        val categoryCardView: CardView = categoryCellOpening.cardView
        val categoryTitleTextView: TextView = categoryCellOpening.titleTextView
        val categoryActionsLinearLayout: LinearLayout = categoryCellOpening.actionsLinearLayout
        val actionAddToLearningLinearLayout: LinearLayout = categoryCellOpening.actionAddToLearningLinearLayout
        val actionShareLinearLayout: LinearLayout = categoryCellOpening.actionShareLinearLayout
        val actionMoreDetailsLinearLayout: LinearLayout = categoryCellOpening.actionMoreDetailsLinearLayout
        val actionDeleteLinearLayout: LinearLayout = categoryCellOpening.actionDeleteLinearLayout

    }
}