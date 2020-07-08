package com.xenous.athenekotlin.data

import com.xenous.athenekotlin.utils.STUDENT_ID_DATABASE_KEY
import com.xenous.athenekotlin.utils.STUDENT_NAME_DATABASE_KEY

data class Student(
    var key: String,
    var name: String
) {
    fun toMap(): Map<String, Any?> = mapOf(
        STUDENT_ID_DATABASE_KEY to key,
        STUDENT_NAME_DATABASE_KEY to name
    )
}