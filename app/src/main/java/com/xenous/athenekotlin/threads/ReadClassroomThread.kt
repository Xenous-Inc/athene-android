package com.xenous.athenekotlin.threads

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xenous.athenekotlin.data.Classroom
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.*
import java.lang.Exception
import java.lang.NullPointerException
import kotlin.String as String

class ReadClassroomThread(
    private val teacherUid: String,
    private val classroomUid: String
) {

    private companion object {
        const val TAG = "ReadClassroomThread"
    }

    interface ReadClassroomResultListener {
        fun onSuccess(classroom: Classroom)

        fun onError(databaseError: DatabaseError)

        fun onFailure(exception: Exception) {}
    }

    private var readClassroomResultListener: ReadClassroomResultListener? = null

    fun setReadClassroomResultListener(readClassroomResultListener: ReadClassroomResultListener) {
        this.readClassroomResultListener = readClassroomResultListener
    }

    fun run() {
        val reference =
            FirebaseDatabase.getInstance().reference
                .child(TEACHER_REFERENCE)
                .child(teacherUid)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val teacherName = snapshot.child(TEACHER_NAME_DATABASE_KEY).value
                if(teacherName == null) {
                    Log.d(TAG, "Teacher Name is null")
                    readClassroomResultListener?.onFailure(NullPointerException("Teacher Name is null"))

                    return
                }
                if(teacherName !is String) {
                    Log.d(TAG, "Name is not string")
                    readClassroomResultListener?.onFailure(TypeCastException("Name is not string"))

                    return
                }

                val classroomName
                        = snapshot.child(CLASSROOMS_DATABASE_KEY)
                        .child(classroomUid)
                        .child(CLASSROOM_NAME_DATABASE_KEY)
                        .value
                if(classroomName == null) {
                    Log.d(TAG, "Classroom name is null")
                    readClassroomResultListener?.onFailure(NullPointerException("Classroom name is null"))

                    return
                }
                if(classroomName !is String) {
                    Log.d(TAG, "Classroom name is not string")
                    readClassroomResultListener?.onFailure(TypeCastException("Classroom name is not string"))

                    return
                }

                val categoriesAddressesList = mutableListOf<String>()
                val categoriesAddressesSnapshot
                        = snapshot
                                .child(CLASSROOMS_DATABASE_KEY)
                                .child(classroomUid)
                                .child(CATEGORY_REFERENCE)
                for(categorySnapshot in categoriesAddressesSnapshot.children) {
                    val categoryAddress = categorySnapshot.value
                    if(categoryAddress != null) {
                        if(categoryAddress is String) {
                            categoriesAddressesList.add(categoryAddress)
                        }
                    }
                }

                val categoriesTitleList = mutableListOf<String>()
                val wordsList = mutableListOf<List<Word>>()

                val categoriesSnapshot = snapshot.child(CATEGORY_REFERENCE)
                for(categoryAddress in categoriesAddressesList) {
                    val categoryTitle = categoriesSnapshot
                        .child(categoryAddress)
                        .child(CLASSROOM_CATEGORY_NAME_DATABASE_KEY)
                        .value

                    if(categoryTitle != null) {
                        if(categoryTitle is String) {
                            categoriesTitleList.add(categoryTitle)
                        }
                    }

                    val wordsInCurrentCategoryList = mutableListOf<Word>()

                    val wordsSnapshot = categoriesSnapshot.child(categoryAddress)
                    for(wordSnapshot in wordsSnapshot.children) {
                        val foreign = wordSnapshot.child(LEARNING_WORD_DATABASE_KEY).value
                        val native = wordSnapshot.child(NATIVE_WORD_DATABASE_KEY).value
                        if(foreign != null && native != null) {
                            if(foreign is String && native is String) {
                                val word = Word(
                                    native,
                                    foreign
                                )

                                wordsInCurrentCategoryList.add(word)
                            }
                        }
                    }

                    wordsList.add(wordsInCurrentCategoryList.toList())
                }

                val classroom = Classroom(
                    teacherName,
                    classroomName,
                    categoriesTitleList.toList(),
                    wordsList.toList()
                )

                readClassroomResultListener?.onSuccess(classroom)
            }

            override fun onCancelled(error: DatabaseError) {
                readClassroomResultListener?.onError(error)
            }
        })
    }
}