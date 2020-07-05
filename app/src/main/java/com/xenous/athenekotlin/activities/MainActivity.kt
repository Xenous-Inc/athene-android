package com.xenous.athenekotlin.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.github.ybq.android.spinkit.SpinKitView
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.fragments.FragmentsViewPagerAdapter
import com.xenous.athenekotlin.storage.categoryArrayList
import com.xenous.athenekotlin.storage.wordArrayList
import com.xenous.athenekotlin.threads.ReadWordsThread
import com.xenous.athenekotlin.utils.ERROR_CODE
import com.xenous.athenekotlin.utils.SUCCESS_CODE

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var spinKitView: SpinKitView
    private lateinit var dotsIndicator: DotsIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinKitView = findViewById(R.id.spinKitView)

//      Optimize View Pager
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = FragmentsViewPagerAdapter(supportFragmentManager, 0)
        viewPager.offscreenPageLimit = 4
        viewPager.currentItem = 1

//      Connect
        dotsIndicator = findViewById(R.id.dotsIndicator)
        dotsIndicator.setViewPager(viewPager)

    }
}