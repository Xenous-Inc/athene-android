package com.xenous.athenekotlin.threads

import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.*

class AddWordThread(
    private val handler: Handler,
    private val word: Word
) {

    private companion object {
        const val TAG = "AddWordThread"
    }

    fun run() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser == null) {
            Log.d(TAG, "Firebase User is null")

            val msg = Message.obtain()
            msg.apply {
                what = ERROR_CODE
            }
            handler.sendMessage(msg)

            return
        }

        val database = FirebaseDatabase.getInstance()
        val wordsReference = database.reference.child(USERS_REFERENCE).child(firebaseUser.uid).child(WORDS_REFERENCE)
        val categoryReference = database.reference.child(USERS_REFERENCE).child(firebaseUser.uid).child(CATEGORY_REFERENCE)

        val sendingWord = Word(
            word.nativeWord,
            word.learningWord,
            word.category,
            word.lastDateCheck,
            word.level,
            wordsReference.push().key
        )

        if(sendingWord.uid == null) {
            Log.d(TAG, "Error while pushing word")
            return
        }

        wordsReference.child(sendingWord.uid).setValue(sendingWord.toMap()).addOnCompleteListener {
            val message = Message.obtain()
            if(it.isSuccessful) {
                Log.d(TAG, "Word has been completely added")

                message.apply {
                    what = SUCCESS_CODE
                }
                handler.sendMessage(message)
            }
            else {
                Log.d(TAG, "Error while adding new word to database")

                message.apply {
                    what = ERROR_CODE
                }
                handler.sendMessage(message)
            }
        }
    }
}