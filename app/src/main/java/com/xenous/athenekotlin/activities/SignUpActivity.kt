package com.xenous.athenekotlin.activities

import android.content.Context
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
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.utils.isEmailValid
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

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
            val toast = Toast(this)
            toast.duration = Toast.LENGTH_LONG
            toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
            toast.view.findViewById<TextView>(R.id.toastTextView).text =
                getString(R.string.sign_up_email_is_invalid)
            toast.show()

            return@OnClickListener
        }
        if(password != confirmPassword) {
            val toast = Toast(this)
            toast.duration = Toast.LENGTH_LONG
            toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
            toast.view.findViewById<TextView>(R.id.toastTextView).text =
                getString(R.string.sign_up_passwords_do_not_match)
            toast.show()

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
                val toast = Toast(this)
                toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                toast.duration = Toast.LENGTH_LONG

                toast.view.findViewById<TextView>(R.id.toastTextView).text = when(it) {
                    is FirebaseAuthUserCollisionException ->
                        getString(R.string.firebase_auth_exception_user_collision_toast_message)
                    is FirebaseNetworkException ->
                        getString(R.string.firebase_auth_exception_network_error_toast_message)
                    is FirebaseAuthWeakPasswordException ->
                        getString(R.string.firebase_auth_exception_weak_password_toast_message)
                    else ->
                        getString(R.string.firebase_auth_exception_unknown_error_toast_message)
                }
                Log.d(TAG, "${it::class.java}: $it")

                toast.show()

                signUpImageView.setImageDrawable(getDrawable(R.drawable.ic_continue_white))
                clickBlocker?.visibility = View.GONE
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
            val isTutorialPassed =
                getSharedPreferences(getString(R.string.shared_pref_tutorial_key), Context.MODE_PRIVATE)
                    .getBoolean(getString(R.string.shared_pref_tutorial_key), false)

            val intent: Intent =
                if(!isTutorialPassed) Intent(this, TutorialActivity::class.java)
                else Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
//            User email isn't verified, send verification letter
        else {
            currentUser.sendEmailVerification()
                .addOnSuccessListener {
                    val toast = Toast(this)
                    toast.duration = Toast.LENGTH_LONG
                    toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                    toast.view.findViewById<TextView>(R.id.toastTextView).text =
                        getString(R.string.verification_successfully_sent_verification_letter)
                    toast.show()

                    val intent = Intent(this, VerificationActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Failed to send verification letter. $exception")

                    val toast = Toast(this)
                    toast.duration = Toast.LENGTH_LONG
                    toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                    toast.view.findViewById<TextView>(R.id.toastTextView).text =
                        getString(R.string.verification_failed_to_send_verification_letter)
                    toast.show()

                    signUpImageView.setImageDrawable(getDrawable(R.drawable.ic_continue_white))
                    clickBlocker?.visibility = View.GONE
                }
        }
    }

//    Google Auth Methods
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null && account.idToken != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
                else {
                    throw NullPointerException("An account or it's idToken is null")
                }
            }
            catch (e: ApiException) {
                val toast = Toast(this)
                toast.duration = Toast.LENGTH_LONG
                toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                toast.view.findViewById<TextView>(R.id.toastTextView).text = when (e.statusCode) {
                    CommonStatusCodes.TIMEOUT ->
                        getString(R.string.api_exception_timeout_toast_message)
                    CommonStatusCodes.NETWORK_ERROR ->
                        getString(R.string.api_exception_network_error_toast_message)
                    CommonStatusCodes.INVALID_ACCOUNT ->
                        getString(R.string.api_exception_invalid_account_toast_message)
                    else ->
                        getString(R.string.api_exception_unknown_error_toast_message)
                }
                toast.show()

                signUpWithGoogleImageView.setImageDrawable(getDrawable(R.drawable.ic_google_sign_in))
                clickBlocker?.visibility = View.GONE
            }
            catch (e: NullPointerException) {
                val toast = Toast(this)
                toast.duration = Toast.LENGTH_LONG
                toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                toast.view.findViewById<TextView>(R.id.toastTextView).text =
                    getString(R.string.api_exception_unknown_error_toast_message)
                toast.show()

                signUpWithGoogleImageView.setImageDrawable(getDrawable(R.drawable.ic_google_sign_in))
                clickBlocker?.visibility = View.GONE
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                try {
                    val currentUser = authResult.user!!
                    updateUI(currentUser)
                }
                catch(e: NullPointerException) {
                    val toast = Toast(this)
                    toast.duration = Toast.LENGTH_LONG
                    toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                    toast.view.findViewById<TextView>(R.id.toastTextView).text =
                        getString(R.string.api_exception_unknown_error_toast_message)
                    toast.show()

                    signUpWithGoogleImageView.setImageDrawable(getDrawable(R.drawable.ic_google_sign_in))
                    clickBlocker?.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                val toast = Toast(this)
                toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                toast.duration = Toast.LENGTH_LONG

                toast.view.findViewById<TextView>(R.id.toastTextView).text = when(it) {
                    is FirebaseAuthInvalidUserException ->
                        getString(R.string.firebase_auth_exception_invalid_user_toast_message)
                    is FirebaseAuthUserCollisionException ->
                        getString(R.string.firebase_auth_exception_user_collision_toast_message)
                    else ->
                        getString(R.string.firebase_auth_exception_unknown_error_toast_message)
                }

                toast.show()

                signInWithGoogleImageView.setImageDrawable(getDrawable(R.drawable.ic_google_sign_in))
                clickBlocker?.visibility = View.GONE
            }
    }
}