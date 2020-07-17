package com.xenous.athenekotlin.threads

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.*


class ReadWordsThread {

    private companion object {
        const val TAG = "ReadWordsThread"
    }

    interface ReadWordsResultListener {
        fun onSuccess(wordsList: List<Word>, categoriesList: List<Category>)

        fun onFailure(exception: Exception)

        fun onError(error: DatabaseError)
    }

    private var downloadWordsResultListener: ReadWordsResultListener? = null

    fun setDownloadWordResultListener(downloadWordsResultListener: ReadWordsResultListener) {
        this.downloadWordsResultListener = downloadWordsResultListener
    }

    fun run() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser == null) {
            Log.d(TAG, "Firebase User is null")
            this.downloadWordsResultListener?.onFailure(Exception("Firebase User is null"))

            return
        }

        val database = FirebaseDatabase.getInstance()
        val reference = database.reference.child(USERS_REFERENCE).child(firebaseUser.uid).child(WORDS_REFERENCE)
        val categoryReference = database.reference.child(USERS_REFERENCE).child(firebaseUser.uid).child(CATEGORY_REFERENCE)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Starting downloading words")

                val wordsMutableList = mutableListOf<Word>()
                val categoriesMutableList = mutableListOf<Category>()

                for(wordSnapshot in snapshot.children) {
                    val word = Word(
                        wordSnapshot.child(NATIVE_WORD_DATABASE_KEY).value as String,
                        wordSnapshot.child(FOREIGN_WORD_DATABASE_KEY).value as String,
                        wordSnapshot.child(WORD_CATEGORY_DATABASE_KEY).value as String,
                        wordSnapshot.child(WORD_LAST_DATE_DATABASE_KEY).value as Long,
                        wordSnapshot.child(WORD_LEVEL_DATABASE_KEY).value as Long,
                        wordSnapshot.key
                    )

                    wordsMutableList.add(word)
                }

                Log.d(TAG, "Words list size is ${wordsMutableList.size}")
                Log.d(TAG, "Completely downloaded all words")
                Log.d(TAG,  "Starting downloading categories")

                categoryReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(categorySnapshot in snapshot.children) {
                            val category = Category(
                                categorySnapshot.value as String,
                                categorySnapshot.key as String
                            )

                            categoriesMutableList.add(category)
                        }

                        Log.d(TAG, "All categories downloaded completely")
                        Log.d(TAG, categoriesMutableList.size.toString())

                       downloadWordsResultListener?.onSuccess(wordsMutableList.toList(), categoriesMutableList.toList())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Error while downloading categories, aborting... Error is ${error.message}")

                        downloadWordsResultListener?.onError(error)
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Error while downloading words, aborting... Error is ${error.message}")

               downloadWordsResultListener?.onError(error)
            }
        })
    }
}