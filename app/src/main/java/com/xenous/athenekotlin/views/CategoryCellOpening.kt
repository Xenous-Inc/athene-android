package com.xenous.athenekotlin.views

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.utils.ANIMATION_DURATION_HALF
import com.xenous.athenekotlin.utils.animateHeightTo

class CategoryCellOpening(val view: View) {

    /*  Initial Block   */
    private var isExpanded = false

    private var collapsedHeight: Int = 1
    private var expandedHeight: Int? = null

    val cardView: CardView = view.findViewById(R.id.categoryCardView)
    val titleTextView: TextView = view.findViewById(R.id.categoryTitleTextView)
    val actionsLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionsLinearLayout)
    val actionAddToLearningLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionAddToLearningLinearLayout)
    val actionShareLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionShareLinearLayout)
    val actionMoreDetailsLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionMoreDetailsLinearLayout)
    val actionDeleteLinearLayout: LinearLayout = view.findViewById(R.id.categoryActionDeleteLinearLayout)

    init {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                expandedHeight = actionsLinearLayout.measuredHeight
                actionsLinearLayout.layoutParams =
                    actionsLinearLayout.layoutParams.apply { height = collapsedHeight }

                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    /*  Methods Block   */
    fun expand() {
        actionsLinearLayout.animateHeightTo(
            expandedHeight!!,
            duration = ANIMATION_DURATION_HALF
        )
    }

    fun collapse() {
        actionsLinearLayout.animateHeightTo(
            collapsedHeight,
            duration = ANIMATION_DURATION_HALF
        )
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