package com.xenous.athenekotlin.threads

import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.ERROR_CODE
import com.xenous.athenekotlin.utils.SUCCESS_CODE
import com.xenous.athenekotlin.utils.USER_REFERENCE

class DeleteWordThread(
    private val handler: Handler,
    private val word: Word?
) : Thread() {

    companion object {
        const val TAG = "DeleteWordThread"
    }

    override fun run() {
        super.run()

        if(word == null) {
            Log.d(TAG, "Word is null")
            sendErrorMessage()

            return
        }
        if(word.uid == null) {
            Log.d(TAG, "Word's UID is null")
            sendErrorMessage()

            return
        }

        val user = FirebaseAuth.getInstance().currentUser

        if(user == null) {
            Log.d(TAG, "User is null")
            sendErrorMessage()

            return
        }

        val reference = FirebaseDatabase.getInstance()
            .reference
            .child(USER_REFERENCE)
            .child(user.uid)
            .child(word.uid)

        reference
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Word has been deleted completely")

                sendSuccessMessage()
            }
            .addOnFailureListener {
                Log.d(TAG, "Error while deleting word. The error is $it")

                sendErrorMessage()
            }
            .addOnCanceledListener {
                Log.d(TAG, "Transaction has been canceled")

                sendErrorMessage()
            }
    }

    private fun sendErrorMessage() {
        val message = Message.obtain()
        message.apply {
            what = ERROR_CODE
        }

        handler.sendMessage(message)
    }

    private fun sendSuccessMessage() {
        val message = Message.obtain()
        message.apply {
            what = SUCCESS_CODE
        }

        handler.sendMessage(message)
    }
}