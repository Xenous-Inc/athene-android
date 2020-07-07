package com.xenous.athenekotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.wordsArrayList
import com.xenous.athenekotlin.views.adapters.ArchiveRecyclerViewAdapter

class ArchiveFragment: Fragment() {

    //-----------------------------Views------------------------------------------------------------
    private lateinit var wordsArchiveRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.fragment_words_archive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wordsArchiveRecyclerView = view.findViewById(R.id.wordsArchiveRecyclerView)
        wordsArchiveRecyclerView.layoutManager = LinearLayoutManager(activity)
        wordsArchiveRecyclerView.adapter = ArchiveRecyclerViewAdapter(
            context!!,
            getArchiveWordList()
        )
    }

    private fun getArchiveWordList(): List<Word> {
        val wordMutableList = mutableListOf<Word>()

        for(word in wordsArrayList) {
            if(word.level == Word.LEVEL_ARCHIVED.toLong()) {
                for(i in 0..100)
                    wordMutableList.add(word)
            }
        }

        return wordMutableList.toList()
    }
}