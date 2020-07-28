package com.xenous.athenekotlin.data

data class Classroom(
    val teacherName: String,
    val teacherKey : String,
    val classroomName: String,
    val classroomKey: String,
    val categoriesTitlesList: List<String>,
    val wordsListsList: List<List<Word>>
) {
}