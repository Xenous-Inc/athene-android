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

class SignUpActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SignUpActivity"
        const val RC_SIGN_IN = 9001
    }

    private var clickBlocker: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        clickBlocker = findViewById(R.id.clickBlocker)

        val signInTextView = findViewById<TextView>(R.id.signInTextView)
        signInTextView.setOnClickListener { finish() }

        val signUpImageView = findViewById<ImageView>(R.id.signUpImageView)
        signUpImageView.setOnClickListener(getSignUpClickListener())

        val signUpWithGoogleImageView = findViewById<ImageView>(R.id.signUpWithGoogleImageView)
        signUpWithGoogleImageView.setOnClickListener(getSignUpWithGoogleClickListener())
    }

//    OnClick Listener
    private fun getSignUpClickListener(): View.OnClickListener = View.OnClickListener {
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

//        Start Loading Animation, Block Clicks
        run {
            val threeBounce = ThreeBounce()
            (it as ImageView).setImageDrawable(threeBounce)
            threeBounce.start()
            clickBlocker?.visibility = View.VISIBLE
        }
//        Create User
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val currentUser = it.user!!
                updateUI(currentUser)
            }
            .addOnFailureListener {
                clickBlocker?.visibility = View.GONE
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
//                todo: handle exception
            }
    }

    private fun getSignUpWithGoogleClickListener(): View.OnClickListener = View.OnClickListener {
//        Start Loading Animation, Block Clicks
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
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun updateUI(currentUser: FirebaseUser) {
//            User email is verified, start MainActivity
        if (currentUser.isEmailVerified) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
//            User email isn't verified, send verification letter
        else {
            currentUser.sendEmailVerification()
                .addOnSuccessListener {
                    Toast
                        .makeText(
                            this,
                            getString(R.string.verification_successfully_sent_verification_letter),
                            Toast.LENGTH_LONG
                        )
                        .show()
                    val intent = Intent(this, VerificationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { exception ->
//                    clickBlocker.visibility = View.GONE
                    Log.d(TAG, "Failed to send verification letter. $exception")
                    Toast
                        .makeText(
                            this,
                            getString(R.string.verification_failed_to_send_verification_letter),
                            Toast.LENGTH_LONG
                        )
                        .show()
                }
        }
    }

//    Google Auth Methods
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN) {
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