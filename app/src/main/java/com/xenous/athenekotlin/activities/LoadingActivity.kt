package com.xenous.athenekotlin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseError
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.ktx.Firebase
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Classroom
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.exceptions.UserIsAlreadyInClassroomException
import com.xenous.athenekotlin.threads.ReadClassroomThread
import com.xenous.athenekotlin.threads.ReadSharingCategoryThread
import com.xenous.athenekotlin.utils.USER_REFERENCE
import com.xenous.athenekotlin.utils.WORD_CATEGORY_DATABASE_KEY


class LoadingActivity : AppCompatActivity() {
    companion object {
        const val TAG = "LoadingActivity"
        const val DYNAMIC_LINK_TAG = "DynamicLink"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        loadCurrentUser()

        // Parse first dynamic link
        val firstDynamicLinkSharedPreferences = getSharedPreferences(getString(R.string.shared_pref_first_dynamic_link_key), Context.MODE_PRIVATE)
        if(
                firstDynamicLinkSharedPreferences.getBoolean(
                    getString(R.string.shared_pref_first_dynamic_link_key),
                    true
                )
        ) {
            FirebaseDynamicLinks
                .getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener {
                    if(it == null) {
                        Log.d(DYNAMIC_LINK_TAG, "PendingDynamicLinkData is null")

                        return@addOnSuccessListener
                    }
                    val link = it.link
                    if(link == null) {
                        Log.d(DYNAMIC_LINK_TAG, "Link is null")

                        return@addOnSuccessListener
                    }

                    val path = link.path
                    if(path == null) {
                        Log.d(DYNAMIC_LINK_TAG, "Path is null")

                        return@addOnSuccessListener
                    }

                    if(path == "/category") {
                        Log.d(DYNAMIC_LINK_TAG, "Receive Add Category Link")

                        val sharingUserUid = link.getQueryParameter(USER_REFERENCE)
                        val categoryTitle = link.getQueryParameter(WORD_CATEGORY_DATABASE_KEY)

                        if(sharingUserUid != null && categoryTitle != null) {
                            val editor = firstDynamicLinkSharedPreferences.edit()
                            editor.putInt(
                                getString(R.string.shared_pref_first_dynamic_link_type_key),
                                0
                            )
                            editor.putString(
                                getString(R.string.shared_pref_first_dynamic_link_user_uid_key),
                                sharingUserUid
                            )
                            editor.putString(
                                getString(R.string.shared_pref_first_dynamic_link_category_key),
                                categoryTitle
                            )
                            editor.apply()
                        }
                    }
                    else if(path == "/invite") {
                        Log.d(DYNAMIC_LINK_TAG, "Receive Invite Link")
                        val teacherUid = link.getQueryParameter("teacherId") // ToDo: Replace to constants
                        val classUid = link.getQueryParameter("classId")

                        if(teacherUid != null && classUid != null) {
                            val editor = firstDynamicLinkSharedPreferences.edit()
                            editor.putInt(
                                getString(R.string.shared_pref_first_dynamic_link_type_key),
                                1
                            )
                            editor.putString(
                                getString(R.string.shared_pref_first_dynamic_link_teacher_uid_key),
                                teacherUid
                            )
                            editor.putString(
                                getString(R.string.shared_pref_first_dynamic_link_classroom_uid_key),
                                classUid
                            )
                            editor.apply()
                        }
                        else {
                            Log.d(DYNAMIC_LINK_TAG, "One or more query parameters are null")
                        }
                    }
                }
                .addOnFailureListener {
                    Log.d(
                        DYNAMIC_LINK_TAG,
                        "Error while receiving dynamic link. The error is ${it.message}"
                    )
                }
                .addOnCanceledListener {
                    Log.d(DYNAMIC_LINK_TAG, "Transaction canceled")
                }
        }
    }

    private fun loadCurrentUser() {
        val currentCachedUser = Firebase.auth.currentUser
        if(currentCachedUser != null) {
            currentCachedUser.reload()
                .addOnSuccessListener {
                    val currentUser = Firebase.auth.currentUser
                    updateUI(currentUser)
                }
                .addOnFailureListener {
                    if(it is FirebaseNetworkException) {
                        val toast = Toast(this)
                        toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                        toast.duration = Toast.LENGTH_SHORT
                        toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.firebase_auth_exception_network_error_toast_message)
                        toast.show()

                        Thread {
                            Thread.sleep(5000)
                            loadCurrentUser()
                        }.start()
                    }
                    else {
                        updateUI(null)
                    }
                }
        }
        else {
            updateUI(currentCachedUser)
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
//        User isn't NULL, check if email verified
        if(currentUser != null) {
//            User email is verified, start MainActivity
            if(currentUser.isEmailVerified) {
                val isTutorialPassed =
                    getSharedPreferences(getString(R.string.shared_pref_tutorial_key), Context.MODE_PRIVATE)
                    .getBoolean(getString(R.string.shared_pref_tutorial_key), false)

                val intent: Intent =
                    if(!isTutorialPassed) Intent(this, TutorialActivity::class.java)
                    else Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
//            User email isn't verified, start VerificationActivity
            else {
                val intent = Intent(this, VerificationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
//        User is NULL, start LoginActivity
        else {
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}