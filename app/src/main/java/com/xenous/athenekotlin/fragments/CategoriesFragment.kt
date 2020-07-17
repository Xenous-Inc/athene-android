package com.xenous.athenekotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.views.CategoriesScrollView
import kotlinx.android.synthetic.main.fragment_categories.*

class CategoriesFragment: Fragment() {

    private lateinit var categoriesScrollView: CategoriesScrollView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoriesScrollView = CategoriesScrollView(activity!!)
        categoriesContentFrameLayout.addView(categoriesScrollView.scrollView)
    }

    fun notifyDataSetChange() {
        categoriesScrollView.notifyDataSetChange()
    }
}