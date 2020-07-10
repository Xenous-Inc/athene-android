package com.xenous.athenekotlin.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.utils.ANIMATION_DURATION_HALF
import com.xenous.athenekotlin.utils.animateHeightTo
import com.xenous.athenekotlin.utils.slideOutToLeft
import kotlinx.android.synthetic.main.layout_category_cell_opening.view.*
import kotlinx.android.synthetic.main.layout_scrollview.view.*

class CategoriesScrollView(rootView: View) {
    private val layoutInflater = LayoutInflater.from(rootView.context)

    val view: ScrollView = layoutInflater.inflate(
        R.layout.layout_scrollview,
        rootView as ViewGroup,
        false
    ) as ScrollView

    private val categoryCellOpeningMutableList = mutableListOf<CategoryCellOpening>()

    fun addDrawData(category: Category) {
//        Init categoryCell
        val categoryCellOpening = CategoryCellOpening.Builder().build(layoutInflater)
        categoryCellOpeningMutableList.add(categoryCellOpening)

//        Configure categoryCell
        categoryCellOpening.view.categoryTitleTextView.text = category.title
        categoryCellOpening.view.categoryCardView.setOnClickListener {_ ->
            /*categoryCellOpening.expand()
            categoryCellOpeningMutableList.forEach { if(it != categoryCellOpening) it.collapse() }*/

            categoryCellOpening.view.slideOutToLeft(duration = ANIMATION_DURATION_HALF, onAnimationEnd = {
                categoryCellOpening.view.visibility = View.INVISIBLE
                categoryCellOpening.view.animateHeightTo(0, duration = ANIMATION_DURATION_HALF)
            })
        }

        /*actionAddToLearningLinearLayout.setOnClickListener {
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
        actionDeleteLinearLayout.setOnClickListener {  }*/

//        Attach categoryCell to scrollView
        view.scrollViewContentLinearLayout.addView(categoryCellOpening.view)

//        Init expanded sizes and hide actionsLinearLayout
        categoryCellOpening.view.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                categoryCellOpening.setExpandedSize(categoryCellOpening.actionsLinearLayout)
                categoryCellOpening.collapse(0)

                categoryCellOpening.view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun addDrawData(categoriesList: ArrayList<Category>) {
        categoriesList.forEach { addDrawData(it) }
    }
}