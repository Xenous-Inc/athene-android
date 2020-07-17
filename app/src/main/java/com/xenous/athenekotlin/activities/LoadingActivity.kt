package com.xenous.athenekotlin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xenous.athenekotlin.R


class LoadingActivity : AppCompatActivity() {
    companion object {
        const val TAG = "LoadingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        loadCurrentUser()
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