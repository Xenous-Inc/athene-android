package com.xenous.athenekotlin.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.xenous.athenekotlin.R

class OpeningView(val view: View, private val outerView: View) {
    companion object {
        const val TAG = "OpeningView"
    }

    /*  Initial Block   */
    var isExpanded = false

    private var collapsedWidth: Int? = null
    private var collapsedHeight: Int? = null
    private var expandedWidth: Int? = null
    private var expandedHeight: Int? = null

    private val categoryCardView = view.findViewById<CardView>(R.id.addWordCategoryCardView)
    private val categoryChosenTextView = view.findViewById<TextView>(R.id.addWordCategoryChosenTextView)
    private val categoriesListConstraintLayout = view.findViewById<ConstraintLayout>(R.id.addWordCategoriesListConstraintLayout)

    init {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                collapsedWidth = categoryCardView.measuredWidth
                collapsedHeight = categoryCardView.measuredHeight

                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        outerView.apply {
            setOnClickListener { if(isExpanded) { collapse() } }
            isClickable = isExpanded
        }
        categoryCardView.setOnClickListener {
            if(!isExpanded) {
                expand()
            }
        }
    }

    /*  Methods Block   */
    fun setExpandedSize(width: Int, height: Int) {
        expandedWidth = width
        expandedHeight = height
    }

    fun setExpandedSize(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                expandedWidth = view.measuredWidth
                expandedHeight = view.measuredHeight

                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    fun expand() {
        if(expandedWidth != null && expandedHeight != null) {
            categoryChosenTextView.animateAlphaTo(expectingAlpha = 0F)
            categoryCardView.animateWidthTo(expandedWidth!!)
            categoryCardView.animateHeightTo(expandedHeight!!)
            categoryCardView.animateCardBackgroundColorTo(Color.WHITE)
            categoriesListConstraintLayout.animateAlphaTo(
                expectingAlpha = 1F,
                delay = ANIMATION_DURATION,
                duration = ANIMATION_DURATION/2,
                onAnimationStart = {
                    categoriesListConstraintLayout.visibility = View.VISIBLE
                },
                onAnimationEnd = {
                    isExpanded = true
                    outerView.isClickable = isExpanded
                }
            )
        }
        else {
            throw Error("Sizes of the expanded view are not indicated")
        }
    }

    fun collapse() {
        categoriesListConstraintLayout.animateAlphaTo(
            expectingAlpha = 0F,
            onAnimationEnd = {
                categoriesListConstraintLayout.visibility = View.GONE
            }
        )
        categoryCardView.animateWidthTo(collapsedWidth!!)
        categoryCardView.animateHeightTo(collapsedHeight!!)
        categoryCardView.animateCardBackgroundColorTo(ContextCompat.getColor(view.context, R.color.colorBackgroundTransparent))
        categoryChosenTextView.animateAlphaTo(
            expectingAlpha = 1F,
            delay = ANIMATION_DURATION,
            duration = ANIMATION_DURATION/2,
            onAnimationEnd = {
                isExpanded = false
                outerView.isClickable = isExpanded
            }
        )
    }

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        categoryCardView.setOnClickListener(onClickListener)
    }

    /*  Builder Block   */
    @SuppressLint("InflateParams")
    class Builder(private val outerView: View, private val context: Context) {
        fun build(layoutInflater: LayoutInflater): OpeningView {
            val view = layoutInflater.inflate(R.layout.layout_opening_view, null)
            return OpeningView(view, outerView)
        }
    }
}