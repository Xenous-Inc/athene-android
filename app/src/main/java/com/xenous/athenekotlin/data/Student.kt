package com.xenous.athenekotlin.data

import com.xenous.athenekotlin.utils.STUDENT_KEY_DATABASE_KEY
import com.xenous.athenekotlin.utils.STUDENT_NAME_DATABASE_KEY
import com.xenous.athenekotlin.utils.STUDENT_NAME_DATABASE_NAME

data class Student(
    private val name: String,
    private val key: String
) {
    fun toMap(): Map<String, Any?>
            = mapOf(
        STUDENT_NAME_DATABASE_NAME to name,
        STUDENT_KEY_DATABASE_KEY to key
    )
}