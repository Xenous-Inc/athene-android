package com.xenous.athenekotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.views.AtheneDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val a = AtheneDialog(this)

    }
}