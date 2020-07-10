package com.xenous.athenekotlin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.views.adapters.CategoryWordsRecyclerViewRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val categoryTitle: String? = intent.getStringExtra(getString(R.string.category_extra))
        val wordArrayList: ArrayList<Word>? = intent.getParcelableArrayListExtra(getString(R.string.words_extra))

        if(categoryTitle == null || wordArrayList == null) {
            onBackPressed()
            return
        }

        categoryTitleTextView.text = categoryTitle

        categoryWordsRecyclerView.adapter =
            CategoryWordsRecyclerViewRecyclerViewAdapter(this, wordArrayList)
        categoryWordsRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}