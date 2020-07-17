package com.xenous.athenekotlin.threads

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.storedInStorageCategoriesArrayList
import com.xenous.athenekotlin.utils.CATEGORY_REFERENCE
import com.xenous.athenekotlin.utils.USERS_REFERENCE
import com.xenous.athenekotlin.utils.WORDS_REFERENCE
import com.xenous.athenekotlin.utils.WORD_CATEGORY_DATABASE_KEY

class DisbandCategoryThread(
    private val category: Category,
    private val wordsInCurrentCategoryList: List<Word>,
    val context: Context
) {
    private companion object {
        const val TAG = "DisbandCategory"
    }

    interface OnDisbandCategoryListener {
        fun onSuccess() {}

        fun onFailure(exception: Exception)

        fun onCanceled() {}
    }

    private var onDisbandCategoryListener: OnDisbandCategoryListener? = null

    fun setOnOnDisbandCategoryListener(onDisbandCategoryListener: OnDisbandCategoryListener) {
        this.onDisbandCategoryListener = onDisbandCategoryListener
    }

    fun run() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser == null) {
            Log.d(TAG, "Firebase User is null")
            this.onDisbandCategoryListener?.onFailure(NullPointerException("Firebase User is null"))

            return
        }

        if(category.uid == null) {
            Log.d(TAG, "Category Key is null")
            this.onDisbandCategoryListener?.onFailure(NullPointerException("Category Key is null"))

            return
        }

        val categoryReference =
            FirebaseDatabase.getInstance().reference
                    .child(USERS_REFERENCE)
                    .child(firebaseUser.uid)
                    .child(CATEGORY_REFERENCE)
                    .child(category.uid)

        categoryReference.removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Category has been removed completely")

                storedInStorageCategoriesArrayList.remove(category)
                onDisbandCategoryListener?.onSuccess()
                disbandWords(firebaseUser)
            }
            .addOnFailureListener {
                Log.d(TAG, "Error while removing category from database. The error is ${it.message}")
                this.onDisbandCategoryListener?.onFailure(it)
            }
            .addOnCanceledListener {
                Log.d(TAG, "Transaction canceled")
                this.onDisbandCategoryListener?.onCanceled()
            }
    }

    private fun disbandWords(firebaseUser: FirebaseUser) {
        val reference
                = FirebaseDatabase.getInstance().reference
                .child(USERS_REFERENCE)
                .child(firebaseUser.uid)
                .child(WORDS_REFERENCE)

        for(word in wordsInCurrentCategoryList) {
            if(word.uid == null) {
                Log.d(TAG, "Word's key is null, skipping it")

                continue
            }

            reference
                .child(word.uid!!)
                .child(WORD_CATEGORY_DATABASE_KEY)
                .setValue(context.getString(R.string.no_category))
                .addOnSuccessListener {
                    Log.d(TAG, "Word's category has been disbanded successfully updated")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Error while disbanding word's category. The error is ${it.message}")
                }
                .addOnCanceledListener {
                    Log.d(TAG, "Transaction canceled")
                }
        }

    }
}