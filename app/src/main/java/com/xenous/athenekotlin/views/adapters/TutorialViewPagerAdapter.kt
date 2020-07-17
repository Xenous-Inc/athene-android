package com.xenous.athenekotlin.views.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.fragments.TutorialFragment

class TutorialViewPagerAdapter(
    fm: FragmentManager,
    behavior: Int
) : FragmentPagerAdapter(fm, behavior) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> TutorialFragment(R.drawable.tutorial_1)
            1 -> TutorialFragment(R.drawable.tutorial_2)
            2 -> TutorialFragment(R.drawable.tutorial_3)
            3 -> TutorialFragment(R.drawable.tutorial_4)
            4 -> TutorialFragment(R.drawable.tutorial_5)
            5 -> TutorialFragment(R.drawable.tutorial_6)
            else -> TutorialFragment(R.drawable.main_background)
        }
    }

    override fun getCount(): Int {
        return 6
    }
}