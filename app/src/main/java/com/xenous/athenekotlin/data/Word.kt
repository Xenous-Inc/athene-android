package com.xenous.athenekotlin.data

import com.google.firebase.database.Exclude
import com.xenous.athenekotlin.utils.forbiddenSymbols

data class Word(
    var nativeWord: String?,
    var learningWord: String?,
    var category: String? = null,
    var lastDateCheck: Long? = 0,
    var level: Long? = 0,
    val uid: String? = null
) {
    companion object {
        const val LEVEL_ADDED = -2
        const val LEVEL_ARCHIVED = -1
        const val LEVEL_DAY = 0
        const val LEVEL_WEEK = 1
        const val LEVEL_MONTH = 2
        const val LEVEL_QUARTER = 3
        const val LEVEL_HALF = 4
        const val LEVEL_YEAR = 5
        val CHECK_INTERVAL = hashMapOf(
            LEVEL_DAY to 86400000L,
            LEVEL_WEEK to 604800000L,
            LEVEL_MONTH to 2592000000L,
            LEVEL_QUARTER to 7776000000L,
            LEVEL_HALF to 15552000000L,
            LEVEL_YEAR to 31104000000L
        )

        const val WORD_IS_NULL = -1
        const val WORD_IS_OK = 0
        const val WORD_CONTAINS_FORBIDDEN_SYMBOLS = 1
        const val WORD_IS_TO_LONG = 2
    }

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

    fun filter(): Int {
        if(nativeWord == null && learningWord == null) {
            return WORD_IS_NULL
        }

        val equawalent = listOf(nativeWord, learningWord)



        for(element in equawalent) {
            if(element!!.length >= 35) {
                return WORD_IS_TO_LONG
            }

            for(forbiddenSymbol in forbiddenSymbols) {
                if(element.contains(forbiddenSymbol)) {
                    return WORD_CONTAINS_FORBIDDEN_SYMBOLS
                }
            }
        }

        return WORD_IS_OK
    }
}