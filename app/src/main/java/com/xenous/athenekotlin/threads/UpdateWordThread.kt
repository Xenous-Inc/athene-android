package com.xenous.athenekotlin.threads

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.USERS_REFERENCE
import com.xenous.athenekotlin.utils.WORDS_REFERENCE
import com.xenous.athenekotlin.utils.WORD_LAST_DATE_DATABASE_KEY
import com.xenous.athenekotlin.utils.WORD_LEVEL_DATABASE_KEY
import java.lang.Exception
import java.lang.NullPointerException

class UpdateWordThread(
    private val word: Word
) {
    private companion object {
        const val TAG = "UpdateWordThread"
    }

    interface UpdateWordThreadListener {
        fun onSuccess()

        fun onFailure(exception: Exception)

        fun onCanceled() { }
    }

    private var updateWordThreadListener: UpdateWordThreadListener? = null

    fun setUpdateWordThreadListener(updateWordThreadListener: UpdateWordThreadListener) {
        this.updateWordThreadListener = updateWordThreadListener
    }

    fun run() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            Log.d(TAG, "Firebase user is null")
            this.updateWordThreadListener?.onFailure(NullPointerException("Firebase user is null"))

            return
        }
        if (word.uid == null) {
            Log.d(TAG, "Word's key is null")
            this.updateWordThreadListener?.onFailure(NullPointerException("Word's key is null"))

            return
        }
        Log.d(TAG, word.level.toString())

        val reference = FirebaseDatabase.getInstance().reference
            .child(USERS_REFERENCE)
            .child(firebaseUser.uid)
            .child(WORDS_REFERENCE)
            .child(word.uid)


        reference.setValue(word.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "Word has been successfully updated")

                this.updateWordThreadListener?.onSuccess()
            }
            .addOnFailureListener {
                Log.d(TAG, "Error while updating word's last date. Error is ${it.message}")

                this.updateWordThreadListener?.onFailure(it)
            }
            .addOnCanceledListener {
                Log.d(TAG, "Transaction canceled")

                this.updateWordThreadListener?.onCanceled()
            }
    }
}