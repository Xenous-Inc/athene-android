package com.xenous.athenekotlin.threads

import android.os.Handler
import android.os.Message
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.utils.CATEGORY_REFERENCE
import com.xenous.athenekotlin.utils.ERROR_CODE
import com.xenous.athenekotlin.utils.SUCCESS_CODE

class AddCategoryThread(
    private val handler: Handler,
    private val category: Category
) {
    private companion object {
        const val TAG = "AddCategoryThread"
    }

    fun run() {
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null) {
            Log.d(TAG, "User is null")

            return
        }

        val reference = FirebaseDatabase.getInstance().reference
            .child(user.uid)
            .child(CATEGORY_REFERENCE)

        val key = reference.push().key

        if(key == null) {
            Log.d(TAG, "Error while pushing category to database")

            return
        }

        val category = Category(
            this.category.category,
            key
        )

        reference.child(category.uid!!).setValue(category.category)
            .addOnSuccessListener {
                Log.d(TAG, "Category has been successfully added to database")

                sendSuccessMessage(category)
            }
            .addOnFailureListener {
                Log.d(TAG, "Error while adding category to database")

                sendErrorMessage()
            }
            .addOnCanceledListener {
                Log.d(TAG, "Transaction has been")
            }

    }

    private fun sendSuccessMessage(category: Category) {
        val message = Message.obtain()
        message.apply {
            what = SUCCESS_CODE
            obj = category
        }

        handler.sendMessage(message)
    }

    private fun sendErrorMessage() {
        val message = Message.obtain()
        message.apply {
            what = ERROR_CODE
            obj = null
        }

        handler.sendMessage(message)
    }
}