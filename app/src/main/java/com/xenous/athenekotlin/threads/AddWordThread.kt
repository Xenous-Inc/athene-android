package com.xenous.athenekotlin.threads

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.*
import java.lang.Exception

class AddWordThread(
    private val word: Word
) {

    private companion object {
        const val TAG = "AddWordThread"
    }

    interface AddWordResultListener {
        fun onSuccess(word: Word)

        fun onFailure(exception: Exception)

        fun onCanceled() {}
    }

    private var addWordResultListener: AddWordResultListener? = null

    fun setAddWordResultListener(addWordResultListener: AddWordResultListener) {
        this.addWordResultListener = addWordResultListener
    }

    fun run() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if(firebaseUser == null) {
            Log.d(TAG, "Firebase User is null")

            this.addWordResultListener?.onFailure(Exception("Firebase User is null"))

            return
        }

        val database = FirebaseDatabase.getInstance()
        val wordsReference = database.reference.child(USERS_REFERENCE).child(firebaseUser.uid).child(WORDS_REFERENCE)

        val key = wordsReference.push().key

        if(key == null) {
            this.addWordResultListener?.onFailure(Exception("Error while pushing the word"))

            return
        }

        val sendingWord = Word(
            word.native,
            word.foreign,
            word.category,
            word.lastDateCheck,
            word.level,
            key
        )

        wordsReference.child(key).setValue(sendingWord.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "Success while adding word to database")

                this.addWordResultListener?.onSuccess(word)
            }
            .addOnFailureListener {
                Log.d(TAG, "Error while adding word to database. The error is ${it.message}")

                this.addWordResultListener?.onFailure(it)
            }
            .addOnCanceledListener {
                Log.d(TAG, "Transaction has been canceled")

                this.addWordResultListener?.onCanceled()
            }
    }
}