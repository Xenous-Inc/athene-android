package com.xenous.athenekotlin.threads

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xenous.athenekotlin.data.Classroom
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.exceptions.UserIsAlreadyInClassroomException
import com.xenous.athenekotlin.utils.*

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

        fun onFailure(exception: Exception)
    }

    private var readClassroomResultListener: ReadClassroomResultListener? = null

    fun setReadClassroomResultListener(readClassroomResultListener: ReadClassroomResultListener) {
        this.readClassroomResultListener = readClassroomResultListener
    }

    fun run() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser == null) {
            Log.d(TAG, "Firebase User is null")
            this.readClassroomResultListener?.onFailure(NullPointerException("Firebase User is null"))

            return
        }

        val reference
                = FirebaseDatabase.getInstance().reference
                .child(TEACHERS_REFERENCE)
                .child(teacherUid)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

//              Check if user exist in classroom
                val studentsSnapshot
                        = snapshot
                            .child(CLASSROOMS_DATABASE_KEY)
                            .child(classroomUid)
                            .child(STUDENTS_REFERENCE)
                for(studentSnapshot in studentsSnapshot.children) {
                    val studentKey = studentSnapshot.value
                    if(studentKey is String) {
                        if(firebaseUser.uid == studentKey) {
                            Log.d(TAG, "Current User already exist")
                            readClassroomResultListener?.onFailure(UserIsAlreadyInClassroomException())

                            break
                        }
                    }
                }

//              Read Teacher name
                val teacherName = snapshot.child(TEACHER_NAME_DATABASE_KEY).value
                if(teacherName !is String) {
                    Log.d(TAG, "Name is not string")
                    readClassroomResultListener?.onFailure(TypeCastException("Name is not string"))

                    return
                }

//              Read Classroom name
                val classroomName
                        = snapshot
                        .child(CLASSROOMS_DATABASE_KEY)
                        .child(classroomUid)
                        .child(CLASSROOM_NAME_DATABASE_KEY)
                        .value

                if(classroomName !is String) {
                    Log.d(TAG, "Classroom name is not string")
                    readClassroomResultListener?.onFailure(TypeCastException("Classroom name is not string"))

                    return
                }

//              Read Categories Addresses
                val categoriesAddressesList = mutableListOf<String>()
                val categoriesAddressesSnapshot
                        = snapshot
                                .child(CLASSROOMS_DATABASE_KEY)
                                .child(classroomUid)
                                .child(CLASSROOM_CATEGORIES_REFERENCE)
                for(categorySnapshot in categoriesAddressesSnapshot.children) {
                    val categoryAddress = categorySnapshot.value
                    if(categoryAddress is String) {
                        categoriesAddressesList.add(categoryAddress)
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


                    if(categoryTitle is String) {
                        categoriesTitleList.add(categoryTitle)
                    }
                    else {
                        continue
                    }

                    val wordsInCurrentCategoryList = mutableListOf<Word>()

                    val wordsSnapshot = categoriesSnapshot
                        .child(categoryAddress)
                        .child(WORDS_REFERENCE)

                    for(wordSnapshot in wordsSnapshot.children) {
                        val foreign = wordSnapshot.child(CLASSROOM_FOREIGN_WORD_DATABASE_KEY).value
                        val native = wordSnapshot.child(CLASSROOM_NATIVE_WORD_DATABASE_KEY).value
                        if(foreign is String && native is String ) {
                            val word = Word(
                                native,
                                foreign,
                                categoryTitle
                            )

                            wordsInCurrentCategoryList.add(word)
                        }
                    }

                    wordsList.add(wordsInCurrentCategoryList.toList())
                }

                val classroom = Classroom(
                    teacherName = teacherName,
                    teacherKey = teacherUid,
                    classroomName = classroomName,
                    classroomKey = classroomUid,
                    categoriesTitlesList = categoriesTitleList.toList(),
                    wordsListsList = wordsList.toList()
                )
                Log.d(TAG, "$classroom")

                readClassroomResultListener?.onSuccess(classroom)
            }

            override fun onCancelled(error: DatabaseError) {
                readClassroomResultListener?.onError(error)
            }
        })
    }
}