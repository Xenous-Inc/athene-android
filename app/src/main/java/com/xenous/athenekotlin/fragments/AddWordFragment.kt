package com.xenous.athenekotlin.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.views.OpeningView

class AddWordFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_word, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val outerFrameLayout = view.findViewById<FrameLayout>(R.id.addWordCategoryOuterFrameLayout)
        val categoryFrameLayout = view.findViewById<FrameLayout>(R.id.addWordCategoryFrameLayout)

        val openingView = OpeningView.Builder(outerFrameLayout).build(layoutInflater)
        openingView.apply {
            setOnClickListener(View.OnClickListener {
//                Clear Input Focus and Hide Keyboard
                view.clearFocus()
                val imm =
                    activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            })
            setExpandedSize(categoryFrameLayout)
            applyToView(categoryFrameLayout)
        }
    }
}