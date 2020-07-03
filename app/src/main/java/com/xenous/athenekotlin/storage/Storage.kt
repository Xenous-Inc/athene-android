package com.xenous.athenekotlin.storage

import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word

val wordArrayList = mutableListOf<Word>()
val checkingWordsArrayList = mutableListOf<Word>()

val categoryArrayList = mutableListOf<Category>()

fun getCategoriesArrayListWithDefault(): MutableList<Category> {
    val defaultList = mutableListOf<Category>()
    defaultList.add(Category("Без категории"))
    defaultList.add(Category("Спорт"))
    defaultList.add(Category("Наука"))
    defaultList.add(Category("Еда"))
    defaultList.add(Category("Транспорт"))
    defaultList.add(Category("Одежда"))
    defaultList.addAll(categoryArrayList)

    return defaultList
}