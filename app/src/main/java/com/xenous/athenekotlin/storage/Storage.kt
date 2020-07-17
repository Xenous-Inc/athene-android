package com.xenous.athenekotlin.storage

import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word

var storedInStorageWordsArrayList = mutableListOf<Word>()
var checkingWordsArrayList = mutableListOf<Word>()

var storedInStorageCategoriesArrayList = mutableListOf<Category>()

fun getCategoriesArrayListWithDefault(): MutableList<Category> {
    val defaultList = mutableListOf<Category>()
    defaultList.add(Category("без категории"))
    defaultList.add(Category("спорт"))
    defaultList.add(Category("наука"))
    defaultList.add(Category("еда"))
    defaultList.add(Category("транспорт"))
    defaultList.add(Category("одежда"))
    defaultList.addAll(storedInStorageCategoriesArrayList)

    return defaultList
}