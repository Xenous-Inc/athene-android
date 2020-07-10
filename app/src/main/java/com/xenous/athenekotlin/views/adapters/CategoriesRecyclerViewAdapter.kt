package com.xenous.athenekotlin.views.adapters

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.CategoryActivity
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.threads.GenerateDynamicLinksThread
import com.xenous.athenekotlin.views.CategoryCellOpening
import java.util.*
import kotlin.collections.ArrayList

class CategoriesRecyclerViewAdapter(
    private val activity: Activity,
    private val categoriesList: List<Category>,
    private val wordsList: List<Word>
) : RecyclerView.Adapter<CategoriesRecyclerViewAdapter.CategoriesRecyclerViewHolder>() {

    private val notNoNameCategories = mutableListOf<Category>()
    private val wordInCategoriesMatrix = mutableListOf<ArrayList<Word>>()
    private val categoryCellOpeningMutableList = mutableListOf<CategoryCellOpening>()

    private val context = activity.applicationContext

    init {
        for(category in categoriesList) {
            if (category.title == context.getString(R.string.no_category)) continue

            val wordInCurrentCategoryMutableList = ArrayList<Word>()
            for (word in wordsList) {
                if (
                    word.category.trim().toLowerCase(Locale.ROOT) ==
                    category.title.trim().toLowerCase(Locale.ROOT)
                ) {
                    wordInCurrentCategoryMutableList.add(word)
                }
            }

            notNoNameCategories.add(category)
            wordInCategoriesMatrix.add(wordInCurrentCategoryMutableList)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoriesRecyclerViewHolder {
        val categoryCellOpening =
            CategoryCellOpening.Builder().build(activity.layoutInflater)
        categoryCellOpeningMutableList.add(categoryCellOpening)

        categoryCellOpening.view.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                categoryCellOpening.setExpandedSize(categoryCellOpening.actionsLinearLayout)
                categoryCellOpening.collapse(0)

                categoryCellOpening.view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        return CategoriesRecyclerViewHolder(categoryCellOpening)
    }

    override fun getItemCount(): Int {
        return notNoNameCategories.size
    }

    override fun onBindViewHolder(holder: CategoriesRecyclerViewHolder, position: Int) {
        holder.bind(notNoNameCategories[position], position)
    }


    inner class CategoriesRecyclerViewHolder(private val categoryCellOpening: CategoryCellOpening) : RecyclerView.ViewHolder(categoryCellOpening.view) {
        private val categoryCardView: CardView = categoryCellOpening.cardView
        private val categoryTitleTextView: TextView = categoryCellOpening.titleTextView
        private val actionsLinearLayout: LinearLayout = categoryCellOpening.actionsLinearLayout
        private val actionAddToLearningLinearLayout: LinearLayout = categoryCellOpening.actionAddToLearningLinearLayout
        private val actionShareLinearLayout: LinearLayout = categoryCellOpening.actionShareLinearLayout
        private val actionMoreDetailsLinearLayout: LinearLayout = categoryCellOpening.actionMoreDetailsLinearLayout
        private val actionDeleteLinearLayout: LinearLayout = categoryCellOpening.actionDeleteLinearLayout

        fun bind(category: Category, position: Int) {
//            categoryCellOpening.collapse(0)

            categoryTitleTextView.text = category.title
            categoryCardView.setOnClickListener {_ ->
                categoryCellOpening.expand()
                categoryCellOpeningMutableList.forEach { if(it != categoryCellOpening) it.collapse() }
            }

            actionAddToLearningLinearLayout.setOnClickListener {
                val wordsToLearnCount = 0
                wordInCategoriesMatrix[position].forEach {
                }
            }
            actionShareLinearLayout.setOnClickListener {
                val generateDynamicLinksThread = GenerateDynamicLinksThread(categoriesList[position].title)
                generateDynamicLinksThread.setGenerateDynamicLinkResultListener(object : GenerateDynamicLinksThread.GenerateDynamicLinkResultListener {
                    override fun onSuccess(shortLink: String?) {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "text/plain"
                        intent.putExtra(
                            Intent.EXTRA_TEXT,
                            shortLink
                        )
                        activity.startActivity(Intent.createChooser(intent, "Share via"))
                    }

                    override fun onFailure(exception: Exception) {
//                    todo: handle error
                    }
                })
                generateDynamicLinksThread.run()
            }
            actionMoreDetailsLinearLayout.setOnClickListener {
                val intent = Intent(
                    activity,
                    CategoryActivity::class.java
                ).putExtra(
                    activity.getString(R.string.category_extra),
                    notNoNameCategories[position].title
                ).putParcelableArrayListExtra(
                    activity.getString(R.string.words_extra),
                    wordInCategoriesMatrix[position]
                )

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    categoryTitleTextView,
                    activity.getString(R.string.category_title_transition_name)
                )
                activity.startActivity(intent, options.toBundle())
            }
            actionDeleteLinearLayout.setOnClickListener {  }
        }
    }
}