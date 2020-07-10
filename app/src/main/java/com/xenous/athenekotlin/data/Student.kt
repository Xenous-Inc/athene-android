package com.xenous.athenekotlin.data

import com.xenous.athenekotlin.utils.STUDENT_NAME_DATABASE_KEY

data class Student(
    private val name: String,
    private val key: String
) {
    fun toMap(): Map<String, Any?>
            = mapOf(
        STUDENT_NAME_DATABASE_KEY to name,
        STUDENT_NAME_DATABASE_KEY to key
    )
}