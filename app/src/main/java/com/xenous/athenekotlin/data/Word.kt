package com.xenous.athenekotlin.data

import com.google.firebase.database.Exclude

data class Word(
    var nativeWord: String?,
    var learningWord: String?,
    var category: String? = null,
    var lastDateCheck: Long? = 0,
    var level: Long? = 0,
    val uid: String? = null
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

    override fun toString(): String {
        return super.toString()
    }
}