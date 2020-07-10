package com.xenous.athenekotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.storage.wordsArrayList
import com.xenous.athenekotlin.utils.slideInFromBottom
import com.xenous.athenekotlin.utils.slideInFromTop
import com.xenous.athenekotlin.views.adapters.CategoriesRecyclerViewAdapter

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

        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView)
        categoriesRecyclerView.slideInFromBottom()
        categoriesRecyclerView.layoutManager = LinearLayoutManager(context)
        categoriesRecyclerView.adapter = CategoriesRecyclerViewAdapter(
            activity!!,
            getCategoriesArrayListWithDefault(),
            wordsArrayList
        )
    }
}