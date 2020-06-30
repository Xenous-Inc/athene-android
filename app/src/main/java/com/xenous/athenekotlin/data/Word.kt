package com.xenous.athenekotlin.data

import com.google.firebase.database.Exclude

data class Word(
    var nativeWord: String?,
    var learningWord: String?,
    var category: String?,
    var lastDateCheck: Long?,
    var level: Long?,
    val uid: String?
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "Russian" to nativeWord,
            "English" to learningWord,
            "category" to category,
            "date" to lastDateCheck,
            "level" to level
        )
    }
}