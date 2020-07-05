package com.xenous.athenekotlin.threads

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.utils.CATEGORY_REFERENCE
import java.lang.Exception

class AddCategoryThread(
    private val category: Category
) {
    private companion object {
        const val TAG = "AddCategoryThread"
    }

    interface AddCategoryResultListener {
        fun onSuccess()

        fun onFailure(exception: Exception)

        fun onCanceled() {}
    }

    private var addCategoryResultListener: AddCategoryResultListener? = null

    fun setAddCategoryResultListener(addCategoryResultListener: AddCategoryResultListener) {
        this.addCategoryResultListener = addCategoryResultListener
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
            this.category.title,
            key
        )

        reference.child(category.uid!!).setValue(category.title)
            .addOnSuccessListener {
                Log.d(TAG, "Category has been successfully added to database")

                this.addCategoryResultListener?.onSuccess()
            }
            .addOnFailureListener {
                Log.d(TAG, "Error while adding category to database")

                this.addCategoryResultListener?.onFailure(it)
            }
            .addOnCanceledListener {
                Log.d(TAG, "Transaction has been")

                this.addCategoryResultListener?.onCanceled()
            }

    }
}