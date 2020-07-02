package com.xenous.athenekotlin.data

data class DownloadWordsResult(
    val wordsList: MutableList<Word>,
    val categoriesList: MutableList<Category>
)