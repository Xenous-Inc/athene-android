package com.xenous.athenekotlin.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class FragmentsViewPagerAdapter(
    fm: FragmentManager,
    behavior: Int
) : FragmentPagerAdapter(fm, behavior) {

    private val addWordFragment = AddWordFragment()
    private val wordsCheckFragmentHolder = WordsCheckFragmentHolder()
    private val categoriesFragment = CategoriesFragment()
    private val archiveFragment = ArchiveFragment()

    override fun getItem(position: Int): Fragment {
        var fragment : Fragment? = null
        when(position) {
            0 -> fragment = addWordFragment
            1 -> fragment = wordsCheckFragmentHolder
            2 -> fragment =categoriesFragment
            3 -> fragment = archiveFragment
        }

        return fragment!!
    }

    override fun notifyDataSetChanged() {
        addWordFragment.notifyDataSetChanged()
        wordsCheckFragmentHolder.notifyDataSetChanged()
        categoriesFragment.notifyDataSetChange()
    }

    override fun getCount(): Int {
        return 4
    }

}