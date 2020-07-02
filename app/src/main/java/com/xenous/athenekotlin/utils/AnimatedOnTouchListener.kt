package com.xenous.athenekotlin.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

@SuppressLint("ClickableViewAccessibility")
val animatedOnTouchListener = View.OnTouchListener { view, motionEvent ->
    when(motionEvent.actionMasked) {
        MotionEvent.ACTION_DOWN -> view.alpha = 0.5F
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.alpha = 1F
    }

    return@OnTouchListener true
}