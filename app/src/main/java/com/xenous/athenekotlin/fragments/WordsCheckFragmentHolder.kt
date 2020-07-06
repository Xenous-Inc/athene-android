package com.xenous.athenekotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Word

class WordsCheckFragmentHolder : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_words_check_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val words = arrayListOf(
            Word(
                "Здоровенное яблоко",
                "Huge apple",
                null,
                null,
                null,
                null
            ),
            Word(
                "Червяк",
                "Worm",
                null,
                null,
                null,
                null
            ),
            Word(
                "Больной на голову",
                "Sick",
                null,
                null,
                null,
                null
            )
        )
        startWordsCheck(words)
    }

    private fun startWordsCheck(
        words: ArrayList<Word>,
        index: Int = 0
    ) {
        val word = if(index < words.size) words[index] else null
        val wordsCheckFragment = WordsCheckFragment(word, isLast = (index == words.size -1))
        wordsCheckFragment.onWordCheckStateChangeListener = object : WordsCheckFragment.OnWordCheckStateChangeListener {
            override fun onWordChecked() {
                if(index <= words.size - 1) {
                    startWordsCheck(words, index + 1)
                }
            }

            override fun onWordsEnd() {
//                If there is no more words and array is not empty
                if(words.isNotEmpty()) {
                    Toast.makeText(context, "CONFETTI", Toast.LENGTH_LONG).show()
//                todo: call confetti view
                }
            }
        }

        val fragmentTransaction = fragmentManager?.beginTransaction()
        if(index == 0) {
            fragmentTransaction?.add(R.id.wordsCheckHolderFrameLayout, wordsCheckFragment)
        }
        else {
            fragmentTransaction?.replace(R.id.wordsCheckHolderFrameLayout, wordsCheckFragment)
        }
        fragmentTransaction?.commit()
    }
}