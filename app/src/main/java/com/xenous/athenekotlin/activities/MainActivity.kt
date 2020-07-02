package com.xenous.athenekotlin.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.viewpager.widget.ViewPager
import com.github.ybq.android.spinkit.SpinKitView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.fragments.FragmentsViewPagerAdapter
import android.view.View
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.xenous.athenekotlin.data.DownloadWordsResult
import com.xenous.athenekotlin.storage.categoryArrayList
import com.xenous.athenekotlin.storage.wordArrayList
import com.xenous.athenekotlin.threads.DownloadWordsThread
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
        dotsIndicator = findViewById<DotsIndicator>(R.id.dotsIndicator)
        dotsIndicator.setViewPager(viewPager)

        DownloadWordsThread(getDownloadWordsHandler()).start()
    }

    private fun getDownloadWordsHandler() = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            if(msg.what == SUCCESS_CODE) {
                if(msg.obj is DownloadWordsResult) {
                    spinKitView.visibility = View.INVISIBLE
                    viewPager.visibility = View.VISIBLE
                    dotsIndicator.visibility = View.VISIBLE

                    val downloadWordsResult = msg.obj as DownloadWordsResult

                    wordArrayList.addAll(downloadWordsResult.wordsList)
//                   ToDo: Find words which need to be checked
                    categoryArrayList.addAll(downloadWordsResult.categoriesList)
                }
            }
            else if(msg.what == ERROR_CODE) {
//               ToDO: Add analyze Error logic
            }
        }
    }
}