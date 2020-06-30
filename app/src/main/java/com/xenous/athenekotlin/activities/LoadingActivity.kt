package com.xenous.athenekotlin.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.data.Category
import com.xenous.athenekotlin.storage.Storage

class LoadingActivity : AppCompatActivity() {
    companion object {
        const val TAG = "LoadingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val currentCachedUser = Firebase.auth.currentUser
        if(currentCachedUser != null) {
            currentCachedUser.reload()
                .addOnSuccessListener {
                    val currentUser = Firebase.auth.currentUser
                    updateUI(currentUser)
                }
                .addOnFailureListener {
//                    User doesn't exist
                    updateUI(null)
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
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
//            User email isn't verified, start VerificationActivity
            else {
                val intent = Intent(this, VerificationActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
//        User is NULL, start LoginActivity
        else {
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
}