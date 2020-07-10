package com.xenous.athenekotlin.threads

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.data.Classroom
import com.xenous.athenekotlin.data.Student
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.getCategoriesArrayListWithDefault
import com.xenous.athenekotlin.utils.*
import java.lang.Exception
import java.lang.NullPointerException

class SynchronizeStudentWithClassroomThread(
    private val classroom: Classroom,
    private val student: Student
) {
    private companion object {
        const val TAG = ""
    }
    interface SynchronizeStudentWithClassroomListener {
        fun onSuccessAddedToClassroom() { }

        fun onSuccessSynchronized() { }

        fun onFailureAddedToClassroom(exception: Exception)

        fun onFailureSynchronized(exception: Exception)

        fun onCanceled() { }
    }

    private var synchronizeStudentWithClassroomListener: SynchronizeStudentWithClassroomListener? = null

    fun setSynchronizeStudentWithClassroomListener(
        synchronizeStudentWithClassroomListener: SynchronizeStudentWithClassroomListener
    ) {
        this.synchronizeStudentWithClassroomListener = synchronizeStudentWithClassroomListener
    }

    fun run() {
        addStudentToClassroom()
        addCategoriesFromClassroom()
        addWordsFromClassroom()
    }

    private fun addStudentToClassroom() {
        val teacherReference
                = FirebaseDatabase.getInstance().reference
                    .child(TEACHERS_REFERENCE)
                    .child(classroom.teacherKey)
                    .child(CLASSROOMS_DATABASE_KEY)
                    .child(classroom.classroomKey)

        val studentsReference = teacherReference.child(STUDENTS_REFERENCE)

        val studentKey = studentsReference.push().key
        if(studentKey == null) {

            return
        }

        studentsReference
            .child(studentKey)
            .setValue(student.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "Student has been added to teacher successfully")

                this.synchronizeStudentWithClassroomListener?.onSuccessAddedToClassroom()
            }
            .addOnFailureListener {
                Log.d(TAG, "Fail while adding student to classroom. The exception is ${it.message}")

                this.synchronizeStudentWithClassroomListener?.onFailureAddedToClassroom(it)
            }
            .addOnCanceledListener {
                Log.d(TAG, "Transaction canceled")

                this.synchronizeStudentWithClassroomListener?.onCanceled()
            }
    }



    private fun addCategoriesFromClassroom() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser == null) {
            Log.d(TAG, "Firebase user is null")
            this.synchronizeStudentWithClassroomListener?.onFailureSynchronized(
                NullPointerException("Firebase user is null")
            )

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
                Log.d(TAG, "Pushed category key is null")
                continue@loop
            }

            categoryReference
                .child(key)
                .setValue(categoryTitle)
                .addOnSuccessListener {
                    Log.d(TAG, "Category has been successfully added to database")

                    this.synchronizeStudentWithClassroomListener?.onSuccessSynchronized()
                }
                .addOnFailureListener {
                    Log.d(TAG, "Error while adding category to database. The error is ${it.message}")

                    this.synchronizeStudentWithClassroomListener?.onFailureSynchronized(it)
                }
                .addOnCanceledListener {
                    Log.d(TAG, "Transaction canceled")

                    this.synchronizeStudentWithClassroomListener?.onCanceled()
                }
        }
    }

    private fun addWordsFromClassroom() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser == null) {
            Log.d(TAG, "Firebase user is null")
            this.synchronizeStudentWithClassroomListener?.onFailureSynchronized(
                NullPointerException("Firebase user is null")
            )

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
                    Log.d(TAG, "Pushed word key is null")

                    continue
                }

                word.uid = key
                word.level = Word.LEVEL_ADDED.toLong()

                wordsReference.setValue(word.toMap())
                    .addOnSuccessListener {
                        Log.d(TAG, "Word has been added to database successfully")

                        this.synchronizeStudentWithClassroomListener?.onSuccessSynchronized()
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "Error while adding word to database. The error is ${it.message}")

                        this.synchronizeStudentWithClassroomListener?.onFailureSynchronized(it)
                    }
                    .addOnCanceledListener {
                        Log.d(TAG, "Transaction Canceled")

                        this.synchronizeStudentWithClassroomListener?.onCanceled()
                    }
            }
    }
}