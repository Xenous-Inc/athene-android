package com.xenous.athenekotlin.fragments

import android.annotation.SuppressLint
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.xenous.athenekotlin.data.Word

class FragmentsViewPagerAdapter(
    fm: FragmentManager,
    behavior: Int
) : FragmentPagerAdapter(fm, behavior) {

    private val addWordFragment = AddWordFragment()
    private val wordsCheckFragment = WordsCheckFragmentTest(
        Word(
            "Здоровенное яблоко",
            "Giant apple",
            "Huge Fruits",
            null,
            null,
            null
        ),
        @SuppressLint("HandlerLeak")
        object : Handler() {}
    )
    private val categoriesFragment = CategoriesFragment()
    private val archiveFragment = ArchiveFragment()

    override fun getItem(position: Int): Fragment {
        var fragment : Fragment? = null
        when(position) {
            0 -> fragment = addWordFragment
            1 -> fragment = wordsCheckFragment
            2 -> fragment =categoriesFragment
            3 -> fragment = archiveFragment
        }

        return fragment!!
    }

    override fun getCount(): Int {
        return 4
    }

}