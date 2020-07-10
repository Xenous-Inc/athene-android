package com.xenous.athenekotlin.views

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.utils.ANIMATION_DURATION_HALF
import com.xenous.athenekotlin.utils.animateAlphaTo
import com.xenous.athenekotlin.utils.animateHeightTo

class CategoryCellOpening(val view: View) {

    /*  Initial Block   */
    private var isExpanded = false

    var collapsedHeight: Int = 1
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
        actionsLinearLayout.animateAlphaTo(1F, duration = duration)
        actionsLinearLayout.animateHeightTo(expandedHeight!!, duration = duration)

        Toast.makeText(view.context, "EXPAND $expandedHeight", Toast.LENGTH_SHORT).show()
    }

    fun collapse(duration: Long = ANIMATION_DURATION_HALF) {
        if(duration == 0L) {
            actionsLinearLayout.alpha = 0F
            actionsLinearLayout.layoutParams = actionsLinearLayout.layoutParams.apply {
                height = collapsedHeight
            }
        }
        else {
            actionsLinearLayout.animateAlphaTo(0F, duration = duration)
            actionsLinearLayout.animateHeightTo(collapsedHeight, duration = duration)
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