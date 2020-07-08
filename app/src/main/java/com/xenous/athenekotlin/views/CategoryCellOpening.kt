package com.xenous.athenekotlin.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.CategoryActivity
import com.xenous.athenekotlin.utils.ANIMATION_DURATION_HALF
import com.xenous.athenekotlin.utils.animateHeightTo

class CategoryCellOpening(private val currentActivity: Activity, val view: View) {

    /*  Initial Block   */
    private var isExpanded = false

    private var collapsedHeight: Int = 1
    private var expandedHeight: Int? = null

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
                setOnClickListener()

                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        actionMoreDetailsLinearLayout.setOnClickListener {
            val intent = Intent(view.context, CategoryActivity::class.java)
            val options =
                ActivityOptionsCompat
                    .makeSceneTransitionAnimation(currentActivity, titleTextView, view.context.getString(R.string.category_title_transition_name))
            view.context.startActivity(intent, options.toBundle())
        }
    }

    /*  Methods Block   */
    fun setOnClickListener(onClickListener: View.OnClickListener? = null) {
        view.setOnClickListener {
            expand()
            onClickListener?.onClick(it)
        }
    }

    fun applyToView(view: View) {
        (view as ViewGroup).addView(this.view)
    }

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

    private fun startActivity(currentActivity: Activity) {
        val intent = Intent(view.context, CategoryActivity::class.java)
        val textView: TextView = view.findViewById(R.id.categoryTitleTextView)
        val options =
            ActivityOptionsCompat
                .makeSceneTransitionAnimation(currentActivity, textView, "categoryTitle")
        view.context.startActivity(intent, options.toBundle())
    }

    /*  Builder Block   */
    @SuppressLint("InflateParams")
    class Builder {
        fun build(currentActivity: Activity): CategoryCellOpening {
            val view = currentActivity.layoutInflater.inflate(R.layout.layout_category_cell_opening, null)
            return CategoryCellOpening(currentActivity, view)
        }
    }
}