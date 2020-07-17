package com.xenous.athenekotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.MainActivity
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.checkingWordsArrayList
import com.xenous.athenekotlin.threads.UpdateWordThread
import com.xenous.athenekotlin.views.AtheneDialog

class WordsCheckFragmentHolder : Fragment() {

    var wordsCheckFragment: WordsCheckFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_words_check_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startWordsCheck(checkingWordsArrayList)
    }

    private fun startWordsCheck(
        words: MutableList<Word>,
        index: Int = 0
    ) {
        val word = if(index < words.size) words[index] else null
        wordsCheckFragment = WordsCheckFragment(word, isLast = (index == words.size -1))
        wordsCheckFragment!!.onWordCheckStateChangeListener = object : WordsCheckFragment.OnWordCheckStateChangeListener {
            override fun onWordChecked() {
                if(index <= words.size - 1) {
                    startWordsCheck(words, index + 1)
                }
            }

            override fun onWordDelete() {
                wordsCheckFragment!!.clearFragmentAfterWord {
                    if(index <= words.size - 1) {
                        startWordsCheck(words, index + 1)
                    }
                }
            }

            override fun onRightAnswer(word: Word) {
                updateLevel(word)
            }

            override fun onWordMistake(word: Word) {
                updateLevel(word)
            }

            override fun onWordsEnd() {
//                If there is no more words and array is not empty
                if(words.isNotEmpty()) {
                    val mainActivity = activity as MainActivity
                    mainActivity.callConfetti()
                }
            }
        }

        val fragmentTransaction = fragmentManager?.beginTransaction()
        if(index == 0) {
            fragmentTransaction?.add(R.id.wordsCheckHolderFrameLayout, wordsCheckFragment!!)
        }
        else {
            fragmentTransaction?.replace(R.id.wordsCheckHolderFrameLayout, wordsCheckFragment!!)
        }
        fragmentTransaction?.commit()
    }

    private fun updateLevel(word: Word) {
        val updateWordThread = UpdateWordThread(word)
        updateWordThread.setUpdateWordThreadListener(object : UpdateWordThread.UpdateWordThreadListener {
            override fun onSuccess() { }

            override fun onFailure(exception: Exception) {
                val atheneDialog = AtheneDialog(context!!)
                atheneDialog.apply {
                    message = getString(R.string.edit_word_error_while_updating_message)
                    positiveText = getString(R.string.ok)
                    build()
                }
                atheneDialog.show()
            }
        })
        updateWordThread.run()
    }

    fun notifyDataSetChanged() {
        if(wordsCheckFragment != null) {
            wordsCheckFragment!!.notifyDataSetChanged()
        }
    }
}