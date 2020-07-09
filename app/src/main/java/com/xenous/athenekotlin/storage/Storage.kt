package com.xenous.athenekotlin.storage

import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word

var wordsArrayList = mutableListOf<Word>()
var checkingWordsArrayList = mutableListOf<Word>()

var categoriesArrayList = mutableListOf<Category>()

fun getCategoriesArrayListWithDefault(): MutableList<Category> {
    val defaultList = mutableListOf<Category>()
    defaultList.add(Category("без категории"))
    defaultList.add(Category("спорт"))
    defaultList.add(Category("наука"))
    defaultList.add(Category("еда"))
    defaultList.add(Category("транспорт"))
    defaultList.add(Category("одежда"))
    defaultList.addAll(categoriesArrayList)

    return defaultList
}