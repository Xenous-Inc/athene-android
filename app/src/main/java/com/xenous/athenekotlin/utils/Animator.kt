package com.xenous.athenekotlin.utils

import android.animation.Animator
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import com.xenous.athenekotlin.R

const val ANIMATION_DURATION = 400L
const val ANIMATION_DURATION_HALF = ANIMATION_DURATION/2
const val ANIMATION_DURATION_TWO_THIRDS = ANIMATION_DURATION/3*2

fun View.animateHeightTo(
    expectingHeight: Int,
    delay: Long = 0,
    duration: Long = ANIMATION_DURATION,
    onAnimationEnd: (() -> Unit)? = null
) {
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
        this.startDelay = delay
        this.duration = duration
        this.start()
    }
}

fun View.animateWidthTo(
    expectingWidth: Int,
    delay: Long = 0,
    duration: Long = ANIMATION_DURATION,
    onAnimationEnd: (() -> Unit)? = null
) {
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
        this.startDelay = delay
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

fun View.slideInFromLeft(
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_left)

    animation.apply {
        this.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                onAnimationStart?.let { it() }
            }

            override fun onAnimationRepeat(p0: Animation?) {  }

            override fun onAnimationEnd(p0: Animation?) {
                onAnimationEnd?.let { it() }
            }
        })
        this.duration = duration
    }
    startAnimation(animation)
}

fun View.slideInFromRight(
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val view = this
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_in_from_right)

    animation.apply {
        this.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                onAnimationStart?.let { it() }
            }

            override fun onAnimationRepeat(p0: Animation?) {  }

            override fun onAnimationEnd(p0: Animation?) {
                onAnimationEnd?.let { it() }
            }
        })
        this.duration = duration
    }
    view.startAnimation(animation)
}

fun View.slideInFromTop(
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val view = this
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_in_from_top)

    animation.apply {
        this.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                onAnimationStart?.let { it() }
            }

            override fun onAnimationRepeat(p0: Animation?) {  }

            override fun onAnimationEnd(p0: Animation?) {
                onAnimationEnd?.let { it() }
            }
        })
        this.duration = duration
    }
    view.startAnimation(animation)
}

fun View.slideInFromBottom(
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val view = this
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_in_from_bottom)

    animation.apply {
        this.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                onAnimationStart?.let { it() }
            }

            override fun onAnimationRepeat(p0: Animation?) {  }

            override fun onAnimationEnd(p0: Animation?) {
                onAnimationEnd?.let { it() }
            }
        })
        this.duration = duration
    }
    view.startAnimation(animation)
}

fun View.slideOutToBottom(
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val view = this
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_out_to_bottom)

    animation.apply {
        this.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                onAnimationStart?.let { it() }
            }

            override fun onAnimationRepeat(p0: Animation?) {  }

            override fun onAnimationEnd(p0: Animation?) {
                onAnimationEnd?.let { it() }
            }
        })
        this.duration = duration
    }
    if(view.isVisible) {
        view.startAnimation(animation)
    }
}

fun View.slideOutToLeft(
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val view = this
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_out_to_left)

    animation.apply {
        this.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                onAnimationStart?.let { it() }
            }

            override fun onAnimationRepeat(p0: Animation?) {  }

            override fun onAnimationEnd(p0: Animation?) {
                onAnimationEnd?.let { it() }
            }
        })
        this.duration = duration
    }
    if(view.isVisible) {
        view.startAnimation(animation)
    }
}

fun View.slideOutToRight(
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val view = this
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.slide_out_to_right)

    animation.apply {
        this.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                onAnimationStart?.let { it() }
            }

            override fun onAnimationRepeat(p0: Animation?) {  }

            override fun onAnimationEnd(p0: Animation?) {
                onAnimationEnd?.let { it() }
            }
        })
        this.duration = duration
    }
    if(view.isVisible) {
        view.startAnimation(animation)
    }
}

fun CardView.animateCardBackgroundColorTo(
    color: Int,
    delay: Long = 0,
    duration: Long = ANIMATION_DURATION,
    onAnimationEnd: (() -> Unit)? = null
) {
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
        this.startDelay = delay
        this.duration = duration
        this.start()
    }
}

fun TextView.animateTextColorTo(
    color: Int,
    duration: Long = ANIMATION_DURATION,
    onAnimationEnd: (() -> Unit)? = null
) {
    val textView = this
    val valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), textView.textColors.defaultColor, color)

    valueAnimator.apply {
        this.addUpdateListener {
            textView.setTextColor(it.animatedValue as Int)
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

fun TextView.animateStrikeThroughText(
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val textView = this
    val span = SpannableString(textView.text)
    val strikeThroughSpan = StrikethroughSpan()
    val valueAnimator = ValueAnimator.ofInt(textView.text.length)

    valueAnimator.apply {
        this.addUpdateListener {
            span.setSpan(strikeThroughSpan, 0, it.animatedValue as Int, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = span
            invalidate()
        }
        this.addListener { object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                onAnimationStart?.let { it() }
            }

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

fun TextView.animateRemoveStrikeThroughText(
    duration: Long = ANIMATION_DURATION,
    onAnimationStart: (() -> Unit)? = null,
    onAnimationEnd: (() -> Unit)? = null
) {
    val textView = this
    val span = SpannableString(textView.text)
    val strikeThroughSpan = StrikethroughSpan()
    val valueAnimator = ValueAnimator.ofInt(textView.text.length, 0)

    valueAnimator.apply {
        this.addUpdateListener {
            span.setSpan(strikeThroughSpan, 0, it.animatedValue as Int, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = span
            invalidate()
        }
        this.addListener { object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                onAnimationStart?.let { it() }
            }

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