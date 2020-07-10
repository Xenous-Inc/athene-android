package com.xenous.athenekotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.views.CategoriesScrollView
import kotlinx.android.synthetic.main.fragment_categories.*

class CategoriesFragment: Fragment() {

    private lateinit var categoriesRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesScrollView = CategoriesScrollView(categoriesContentFrameLayout)
        categoriesScrollView.addDrawData(getCategoriesArrayListWithDefault() as ArrayList<Category>)

        categoriesContentFrameLayout.addView(categoriesScrollView.view)

        /*categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView)
        categoriesRecyclerView.layoutManager = LinearLayoutManager(context)
        categoriesRecyclerView.adapter = CategoriesRecyclerViewAdapter(
            activity!!,
            getCategoriesArrayListWithDefault(),
            wordsArrayList
        )*/
    }
}