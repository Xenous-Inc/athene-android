package com.xenous.athenekotlin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xenous.athenekotlin.R

class RegisterActivity : AppCompatActivity() {
    companion object {
        const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val signInTextView = findViewById<TextView>(R.id.signInTextView)
        signInTextView.setOnClickListener { finish() }

        val signUpImageView = findViewById<ImageView>(R.id.signUpImageView)
        signUpImageView.setOnClickListener(getRegisterClickListener())
    }

    private fun getRegisterClickListener(): View.OnClickListener = View.OnClickListener {
        val emailEditText = findViewById<EditText>(R.id.signUpEmailEditText)
        val passwordEditText = findViewById<EditText>(R.id.signUpPasswordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.signUpConfirmPasswordEditText)

        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if(!isEmailValid(email)) {
            Toast
                .makeText(this, getString(R.string.sign_up_email_is_invalid), Toast.LENGTH_LONG)
                .show()
            return@OnClickListener
        }
        if(password != confirmPassword) {
            Toast
                .makeText(this, getString(R.string.sign_up_passwords_do_not_match), Toast.LENGTH_LONG)
                .show()
            return@OnClickListener
        }

        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val currentUser = it.user
                updateUI(currentUser)
            }
            .addOnFailureListener {
                Toast.makeText(this, "AAAAAAAAAAA $it", Toast.LENGTH_LONG).show()
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
//            User email isn't verified, send verification letter
            else {
                currentUser.sendEmailVerification()
                    .addOnSuccessListener {
                        Toast
                            .makeText(this, getString(R.string.verification_successfully_sent_verification_letter), Toast.LENGTH_LONG)
                            .show()
                        val intent = Intent(this, VerificationActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Failed to send verification letter. $exception")
                        Toast
                            .makeText(this, getString(R.string.verification_failed_to_send_verification_letter), Toast.LENGTH_LONG)
                            .show()
                    }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean =  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}