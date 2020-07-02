package com.xenous.athenekotlin.views

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.utils.*

class OpeningView(val view: View, private val outerView: View) {
    companion object {
        const val TAG = "OpeningView"
    }

    /*  Initial Block   */
    private var isExpanded = false
    private var isRunning = false

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
            setOnClickListener { collapse() }
            isClickable = isExpanded
        }
        categoryCardView.setOnClickListener { expand() }
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

    fun setOnClickListener(onClickListener: View.OnClickListener) {
        categoryCardView.setOnClickListener {
            expand()
            onClickListener.onClick(it)
        }
    }

    fun applyToView(view: View) {
        (view as ViewGroup).addView(this.view)
    }

    private fun expand() {
        if(!isExpanded && !isRunning) {
            if(expandedWidth != null && expandedHeight != null) {
                categoryChosenTextView.animateAlphaTo(expectingAlpha = 0F, onAnimationStart = {
                    isRunning = true
                })
                categoryCardView.animateWidthTo(expandedWidth!!)
                categoryCardView.animateHeightTo(expandedHeight!!)
                categoryCardView.animateCardBackgroundColorTo(Color.WHITE)
                categoriesListConstraintLayout.animateAlphaTo(
                    expectingAlpha = 1F,
                    delay = ANIMATION_DURATION/2,
                    duration = ANIMATION_DURATION /2,
                    onAnimationStart = {
                        categoriesListConstraintLayout.visibility = View.VISIBLE
                    },
                    onAnimationEnd = {
                        isRunning = false
                        isExpanded = true
                        outerView.isClickable = isExpanded
                    }
                )
            }
            else {
                throw Error("Sizes of the expanded view are not indicated")
            }
        }
    }

    private fun collapse() {
        if(isExpanded && !isRunning) {
            if(collapsedWidth != null && collapsedHeight != null) {
                categoriesListConstraintLayout.animateAlphaTo(
                    expectingAlpha = 0F,
                    onAnimationStart = {
                      isRunning = true
                    },
                    onAnimationEnd = {
                        categoriesListConstraintLayout.visibility = View.GONE
                    }
                )
                categoryCardView.animateWidthTo(collapsedWidth!!)
                categoryCardView.animateHeightTo(collapsedHeight!!)
                categoryCardView.animateCardBackgroundColorTo(ContextCompat.getColor(view.context, R.color.colorBackgroundTransparent))
                categoryChosenTextView.animateAlphaTo(
                    expectingAlpha = 1F,
                    delay = ANIMATION_DURATION/2,
                    duration = ANIMATION_DURATION /2,
                    onAnimationEnd = {
                        isRunning = false
                        isExpanded = false
                        outerView.isClickable = isExpanded
                    }
                )
            }
            else {
                throw Error("Sizes of the collapsed view are not indicated")
            }
        }
    }

    /*  Builder Block   */
    @SuppressLint("InflateParams")
    class Builder(private val outerView: View) {
        fun build(layoutInflater: LayoutInflater): OpeningView {
            val view = layoutInflater.inflate(R.layout.layout_opening_view, null)
            return OpeningView(view, outerView)
        }
    }
}