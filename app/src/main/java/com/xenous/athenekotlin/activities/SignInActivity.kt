package com.xenous.athenekotlin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.utils.isEmailValid

class SignInActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SignInActivity"
    }

    private var clickBlocker: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        clickBlocker = findViewById(R.id.clickBlocker)

        val signUpTextView = findViewById<TextView>(R.id.signUpTextView)
        signUpTextView.setOnClickListener { startActivity(Intent(this, SignUpActivity::class.java)) }

        val signInImageView = findViewById<ImageView>(R.id.signInImageView)
        signInImageView.setOnClickListener(getSignInClickListener())

        val signInWithGoogleImageView = findViewById<ImageView>(R.id.signInWithGoogleImageView)
        signInWithGoogleImageView.setOnClickListener(getSignInWithGoogleClickListener())
    }

    //    OnClick Listener
    private fun getSignInClickListener(): View.OnClickListener = View.OnClickListener {
        val emailEditText = findViewById<EditText>(R.id.signInEmailEditText)
        val passwordEditText = findViewById<EditText>(R.id.signInPasswordEditText)

        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if(!isEmailValid(email)) {
            Toast
                .makeText(this, getString(R.string.sign_up_email_is_invalid), Toast.LENGTH_LONG)
                .show()
            return@OnClickListener
        }

//        Start Loading Animation
        run {
            val threeBounce = ThreeBounce()
            (it as ImageView).setImageDrawable(threeBounce)
            threeBounce.start()
            clickBlocker?.visibility = View.VISIBLE
        }
//        Sign In User
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val currentUser = it.user!!
                updateUI(currentUser)
            }
            .addOnFailureListener {
                clickBlocker?.visibility = View.GONE
//                todo: handle error
            }
    }

    private fun getSignInWithGoogleClickListener(): View.OnClickListener = View.OnClickListener {
//        Start Loading Animation
        run {
            val threeBounce = ThreeBounce()
            (it as ImageView).setImageDrawable(threeBounce)
            threeBounce.start()
            clickBlocker?.visibility = View.VISIBLE
        }
//        Configure Google Sign In Options
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SignUpActivity.RC_SIGN_IN)
    }

    private fun updateUI(currentUser: FirebaseUser) {
        if(currentUser.isEmailVerified) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        else {
            currentUser.sendEmailVerification()
                .addOnSuccessListener {
                    Toast
                        .makeText(
                            this,
                            getString(R.string.verification_successfully_sent_verification_letter),
                            Toast.LENGTH_LONG
                        ).show()
                    val intent = Intent(this, VerificationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { exception ->
//                    todo: handle error
                    clickBlocker?.visibility = View.GONE
                    Log.d(SignUpActivity.TAG, "Failed to send verification letter. $exception")
                    Toast
                        .makeText(
                            this,
                            getString(R.string.verification_failed_to_send_verification_letter),
                            Toast.LENGTH_LONG
                        ).show()
                }
        }
    }

    //    Google Auth Methods
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == SignUpActivity.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            }
            catch(e: Exception) {
                clickBlocker?.visibility = View.GONE
//                todo: handle error
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val currentUser = authResult.user!!
                updateUI(currentUser)
            }
            .addOnFailureListener {
                clickBlocker?.visibility = View.GONE
//                todo: handle error
            }
    }
}