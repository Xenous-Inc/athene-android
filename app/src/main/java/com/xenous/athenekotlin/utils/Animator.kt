package com.xenous.athenekotlin.utils

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.animation.addListener

const val ANIMATION_END = 9084
const val DELAY_END = 9085


fun View.animateHeightTo(expectingHeight: Int, onAnimationEnd: (() -> Unit)? = null, duration: Long = ANIMATION_DURATION) {
    val view = this
    val valueAnimator = ValueAnimator.ofInt(view.measuredHeight, expectingHeight)

    valueAnimator.apply {
        this.addUpdateListener {
            view.layoutParams = view.layoutParams.apply { height = it.animatedValue as Int }
        }
        this.addListener { object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {  }

            override fun onAnimationRepeat(p0: Animator?) {  }

            override fun onAnimationCancel(p0: Animator?) {  }

            override fun onAnimationEnd(p0: Animator?) {
                onAnimationEnd?.let { it() }
            }
        } }
        this.duration = duration
        this.start()
    }
}

fun View.animateWidthTo(expectingWidth: Int, onAnimationEnd: (() -> Unit)? = null, duration: Long = ANIMATION_DURATION) {
    val view = this
    val valueAnimator = ValueAnimator.ofInt(view.measuredWidth, expectingWidth)

    valueAnimator.apply {
        this.addUpdateListener {
            view.layoutParams = view.layoutParams.apply { width = it.animatedValue as Int }
        }
        this.addListener { object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {  }

            override fun onAnimationRepeat(p0: Animator?) {  }

            override fun onAnimationCancel(p0: Animator?) {  }

            override fun onAnimationEnd(p0: Animator?) {
                onAnimationEnd?.let { it() }
            }
        } }
        this.duration = duration
        this.start()
    }
}

fun CardView.animateCardBackgroundColorTo(color: Int, onAnimationEnd: (() -> Unit)? = null, duration: Long = ANIMATION_DURATION) {
    val cardView = this
    val valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), cardView.cardBackgroundColor.defaultColor, color)

    valueAnimator.apply {
        this.addUpdateListener {
            cardView.setCardBackgroundColor(it.animatedValue as Int)
        }
        this.addListener { object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {  }

            override fun onAnimationRepeat(p0: Animator?) {  }

            override fun onAnimationCancel(p0: Animator?) {  }

            override fun onAnimationEnd(p0: Animator?) {
                onAnimationEnd?.let { it() }
            }
        } }
        this.duration = duration
        this.start()
    }
}

fun View.animateAlphaTo(
    expectingAlpha: Float,
    delay: Long = 0,
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val view = this

    view.animate().apply {
        this.withStartAction(onAnimationStart)
        this.withEndAction(onAnimationEnd)
        this.alpha(expectingAlpha)
        this.startDelay = delay
        this.duration = duration
        this.start()
    }
}