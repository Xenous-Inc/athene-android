package com.xenous.athenekotlin.data

data class Classroom(
    val teacherName: String,
    val classroomName: String,
    val categoriesTitlesList: List<String>,
    val wordsListsList: List<List<Word>>
) {
}