package com.xenous.athenekotlin.threads

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.data.Classroom
import com.xenous.athenekotlin.data.Student
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.utils.*

class SynchronizeStudentWithClassroomThread(
    private val teacherKey: String,
    private val classroomKey: String,
    private val student: Student,
    private val classroom: Classroom
) {
    interface SynchronizeStudentWithClassroomListener {

    }

    fun run() {




    }

    private fun addStudentToClassroom() {
        val teacherReference
                = FirebaseDatabase.getInstance().reference
                    .child(TEACHERS_REFERENCE)
                    .child(teacherKey)
                    .child(CLASSROOMS_DATABASE_KEY)
                    .child(classroomKey)

        val studentsReference = teacherReference.child(STUDENTS_REFERENCE)

        val studentKey = studentsReference.push().key
        if(studentKey == null) {

            return
        }

        studentsReference
            .child(studentKey)
            .setValue(student.toMap())
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
            .addOnCanceledListener {

            }
    }



    private fun addCategoriesFromClassroom() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser == null) {

            return
        }

        val categoryReference
                = FirebaseDatabase.getInstance().reference
                    .child(USERS_REFERENCE)
                    .child(firebaseUser.uid)
                    .child(CATEGORY_REFERENCE)

        loop@
        for(categoryTitle in classroom.categoriesTitlesList) {
            for(defaultCategory in getCategoriesArrayListWithDefault()) {
                if(defaultCategory.title == categoryTitle) {
                    continue@loop
                }
            }

            val key = categoryReference.push().key
            if(key == null) {

                continue@loop
            }

            //ToDo: Add parsing
            categoryReference
                .setValue(categoryTitle)
                .addOnSuccessListener {

                }
                .addOnFailureListener {

                }
                .addOnCanceledListener {

                }
        }
    }

    private fun addWordsFromClassroom() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser == null) {

            return
        }

        val wordsReference
                        = FirebaseDatabase.getInstance().reference
                            .child(USERS_REFERENCE)
                            .child(firebaseUser.uid)
                            .child(WORDS_REFERENCE)

        for(wordList in classroom.wordsListsList)
            for(word in wordList) {
                val key = wordsReference.push().key
                if(key == null) {

                    continue
                }

                word.uid = key
                word.level = Word.LEVEL_ADDED.toLong()

                wordsReference.setValue(word.toMap())
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {

                    }
                    .addOnCanceledListener {

                    }
            }
    }
}