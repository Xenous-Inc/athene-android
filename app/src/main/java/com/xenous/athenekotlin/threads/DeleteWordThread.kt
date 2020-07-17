package com.xenous.athenekotlin.threads

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.storedInStorageWordsArrayList
import com.xenous.athenekotlin.utils.USERS_REFERENCE
import com.xenous.athenekotlin.utils.WORDS_REFERENCE

class DeleteWordThread(
    private val word: Word?
) {

    companion object {
        const val TAG = "DeleteWordThread"
    }

    interface DeleteWordResultListener {
        fun onSuccess()

        fun onFailure(exception: Exception)

        fun onCanceled() {}
    }

    private var deleteWordResultListener: DeleteWordResultListener? = null

    fun setDeleteWordResultListener(deleteWordResultListener: DeleteWordResultListener) {
        this.deleteWordResultListener = deleteWordResultListener
    }

    fun run() {
        if(word == null) {
            Log.d(TAG, "Word is null")
            this.deleteWordResultListener?.onFailure(Exception("Word is null"))

            return
        }
        if(word.uid == null) {
            Log.d(TAG, "Word's UID is null")
            this.deleteWordResultListener?.onFailure(Exception("Word's UID is null"))

            return
        }

        val user = FirebaseAuth.getInstance().currentUser

        if(user == null) {
            Log.d(TAG, "User is null")
            this.deleteWordResultListener?.onFailure(Exception("User is null"))

            return
        }

        val reference = FirebaseDatabase.getInstance()
            .reference
            .child(USERS_REFERENCE)
            .child(user.uid)
            .child(WORDS_REFERENCE)
            .child(word.uid!!)


        reference
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Word has been deleted completely")

                storedInStorageWordsArrayList.remove(word)
                this.deleteWordResultListener?.onSuccess()
            }
            .addOnFailureListener {
                Log.d(TAG, "Error while deleting word. The error is $it")

                this.deleteWordResultListener?.onFailure(it)
            }
            .addOnCanceledListener {
                Log.d(TAG, "Transaction has been canceled")

                this.deleteWordResultListener?.onCanceled()
            }
    }
}