package com.xenous.athenekotlin.threads

import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.DownloadWordsResult
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.*


class DownloadWordsThread(
    private val handler: Handler
) : Thread() {

    private companion object {
        const val TAG = "DownloadWordsThread"
    }

    override fun run() {
        super.run()

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser == null) {
            Log.d(TAG, "Firebase User is null. Stopping downloading process...")

            val msg = Message.obtain()
            msg.apply {
                what = SUCCESS_CODE
                obj = null
            }
            handler.sendMessage(msg)

            return
        }

        val database = FirebaseDatabase.getInstance()
        val reference = database.reference.child(USER_REFERENCE).child(firebaseUser.uid)
        val categoryReference = reference.child(CATEGORY_REFERENCE)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Starting downloading words")

                val wordsList = mutableListOf<Word>()
                val categoriesList = mutableListOf<Category>()

                for(wordSnapshot in snapshot.children) {
                    val word = Word(
                        wordSnapshot.child(NATIVE_WORD_DATABASE_KEY).value as String,
                        wordSnapshot.child(LEARNING_WORD_DATABASE_KEY).value as String,
                        wordSnapshot.child(CATEGORY_REFERENCE).value as String,
                        wordSnapshot.child(WORD_LAST_DATE_DATABASE_KEY).value as Long,
                        wordSnapshot.child(WORD_LEVEL_DATABASE_KEY).value as Long,
                        wordSnapshot.key as String
                    )

                    wordsList.add(word)
                }

                Log.d(TAG, "Completely downloaded all words")
                Log.d(TAG,  "Starting downloading categories")

                categoryReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(categorySnapshot in snapshot.children) {
                            val category = Category(
                                categorySnapshot.key as String,
                                categorySnapshot.value as String
                            )

                            categoriesList.add(category)
                        }

                        Log.d(TAG, "All categories downloaded completely")

                        val downloadWordsResult = DownloadWordsResult(
                            wordsList,
                            categoriesList
                        )

                        val msg = Message.obtain()
                        msg.apply {
                            what = SUCCESS_CODE
                            obj = downloadWordsResult
                        }

                        handler.sendMessage(msg);
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "Error while downloading categories, aborting... Error is ${error.message}")

                        val msg = Message.obtain()
                        msg.apply {
                            what = SUCCESS_CODE
                            obj = null
                        }
                        handler.sendMessage(msg)
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Error while downloading words, aborting... Error is ${error.message}")

                val msg = Message.obtain()
                msg.apply {
                    what = SUCCESS_CODE
                    obj = null
                }
                handler.sendMessage(msg)
            }
        })
    }
}