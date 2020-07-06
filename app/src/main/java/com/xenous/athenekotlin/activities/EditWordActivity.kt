package com.xenous.athenekotlin.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Word

class EditWordActivity(private val word: Word) : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_word)
    }
}