package com.xenous.athenekotlin.threads

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.storage.wordsArrayList
import com.xenous.athenekotlin.utils.*
import java.lang.Exception

class ReadSharingCategoryThread(
    private val sharingUserUid: String,
    private val sharingCategoryTitle: String
) {
    private companion object {
        const val TAG = "SharingCategory"
    }

    interface ReadSharingCategoryListener {
        fun onSuccess(sharingWordsList: List<Word>)

        fun onError(error: DatabaseError)

        fun onFailure(exception: Exception) {}
    }

    private var readSharingCategoryListener: ReadSharingCategoryListener? = null

    fun setReadSharingCategoryListener(readSharingCategoryListener: ReadSharingCategoryListener) {
        this.readSharingCategoryListener = readSharingCategoryListener
    }

    fun run() {
        val reference
                = FirebaseDatabase.getInstance().reference
                    .child(USERS_REFERENCE)
                    .child(sharingUserUid)
                    .child(WORDS_REFERENCE)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(wordsSnapshot: DataSnapshot) {
                val sharingWordsList = mutableListOf<Word>()

                for(wordSnapshot in wordsSnapshot.children) {
                    val categoryTitle = wordSnapshot.child(WORD_CATEGORY_DATABASE_KEY).value
                    if(categoryTitle is String) {
                        val native = wordSnapshot.child(NATIVE_WORD_DATABASE_KEY).value
                        val foreign = wordSnapshot.child(LEARNING_WORD_DATABASE_KEY).value
                        if(native is String && foreign is String) {
                            val word = Word(
                                native,
                                foreign,
                                categoryTitle,
                                0,
                                Word.LEVEL_ADDED.toLong()
                            )

                            var isNew = false
                            for(w in wordsArrayList) {
                                if(w == word) {
                                    isNew = true
                                    break
                                }
                            }
                            if(isNew) {
                                sharingWordsList.add(word)
                            }
                        }
                        else {
                            Log.d(TAG, "Native Word or Foreign are not String")

                            readSharingCategoryListener?.onFailure(TypeCastException(
                                "Native Word or Foreign are not String")
                            )
                        }
                    }
                    else {
                        Log.d(TAG, "Category title is not String")

                        readSharingCategoryListener?.onFailure(
                            TypeCastException("Native Word or Foreign are not String")
                        )
                    }
                }

                readSharingCategoryListener?.onSuccess(sharingWordsList.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Transaction Canceled")

                readSharingCategoryListener?.onError(error)
            }
        })
    }
}