package com.xenous.athenekotlin.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.utils.ANIMATION_DURATION_HALF
import com.xenous.athenekotlin.utils.animateAlphaTo
import com.xenous.athenekotlin.views.adapters.TutorialViewPagerAdapter
import kotlinx.android.synthetic.main.activity_tutorial.*

class TutorialActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        continueImageView.setOnClickListener {
            getSharedPreferences(
                getString(R.string.shared_pref_tutorial_key),
                Context.MODE_PRIVATE
            ).edit().putBoolean(getString(R.string.shared_pref_tutorial_key), true).apply()

            val intent = Intent(this, LoadingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        val tutorialViewPager = findViewById<ViewPager>(R.id.tutorialViewPager)
        val tutorialViewPagerAdapter = TutorialViewPagerAdapter(supportFragmentManager, 0)
        tutorialViewPager.adapter = tutorialViewPagerAdapter
        tutorialViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (position == tutorialViewPagerAdapter.count - 1) {
                    continueImageView.animateAlphaTo(
                        1F,
                        duration = ANIMATION_DURATION_HALF,
                        onAnimationStart = {
                            continueImageView.visibility = View.VISIBLE
                            continueImageView.isClickable = true
                            continueImageView.alpha = 0F
                        },
                        onAnimationEnd = {
                            continueImageView.alpha = 1F
                        }
                    )
                }
                else {
                    continueImageView.animateAlphaTo(
                        0F,
                        duration = ANIMATION_DURATION_HALF,
                        onAnimationStart = {
                            continueImageView.isClickable = false
                            continueImageView.alpha = 1F
                        },
                        onAnimationEnd = {
                            continueImageView.alpha = 0F
                            continueImageView.visibility = View.INVISIBLE
                        }
                    )
                }
            }

            override fun onPageScrollStateChanged(state: Int) {  }
        })

        tutorialDotsIndicator.setViewPager(tutorialViewPager)
    }
}