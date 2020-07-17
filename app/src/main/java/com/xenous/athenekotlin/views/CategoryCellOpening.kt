package com.xenous.athenekotlin.views

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.utils.ANIMATION_DURATION_HALF
import com.xenous.athenekotlin.utils.animateAlphaTo
import com.xenous.athenekotlin.utils.animateHeightTo

class CategoryCellOpening(val view: View) {

    interface OnStateChangeListener {
        fun onExpand() {}

        fun onCollapse() {}
    }

    private var onStateChangeListener: OnStateChangeListener? = null

    fun setOnStateChangeListener(onStateChangeListener: OnStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener
    }

    fun removeOnStateChangeListener() {
        this.onStateChangeListener = null
    }

    /*  Initial Block   */
    var isExpanded = false

    private var collapsedHeight: Int = 1
    private var expandedHeight: Int? = null

    val cardView: CardView = view.findViewById(R.id.categoryCardView)
    val titleTextView: TextView = view.findViewById(R.id.categoryTitleTextView)
    val actionsLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionsLinearLayout)
    val actionAddToLearningLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionAddToLearningLinearLayout)
    val actionShareLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionShareLinearLayout)
    val actionMoreDetailsLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionMoreDetailsLinearLayout)
    val actionDeleteLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionDeleteLinearLayout)

    /*  Methods Block   */
    fun setExpandedSize(view: View) {
        expandedHeight = view.measuredHeight
    }

    fun expand(duration: Long = ANIMATION_DURATION_HALF) {
        actionsLinearLayout.animateHeightTo(expandedHeight!!, duration = duration)
        actionsLinearLayout.animateAlphaTo(1F, duration = duration, onAnimationEnd = {
            isExpanded = true
            onStateChangeListener?.onExpand()
        })
    }

    fun collapse(duration: Long = ANIMATION_DURATION_HALF) {
        if(duration == 0L) {
            actionsLinearLayout.alpha = 0F
            actionsLinearLayout.layoutParams = actionsLinearLayout.layoutParams.apply {
                height = collapsedHeight
            }
            onStateChangeListener?.onCollapse()
            isExpanded = false
        }
        else {
            actionsLinearLayout.animateHeightTo(collapsedHeight, duration = duration)
            actionsLinearLayout.animateAlphaTo(0F, duration = duration, onAnimationEnd = {
                onStateChangeListener?.onCollapse()
                isExpanded = false
            })
        }
    }

    /*  Builder Block   */
    @SuppressLint("InflateParams")
    class Builder {
        fun build(layoutInflater: LayoutInflater): CategoryCellOpening {
            val view = layoutInflater.inflate(R.layout.layout_category_cell_opening, null)
            return CategoryCellOpening(view)
        }
    }
}