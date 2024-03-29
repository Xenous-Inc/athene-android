package com.xenous.athenekotlin.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.firebase.auth.FirebaseAuth
import com.xenous.athenekotlin.R
import kotlinx.android.synthetic.main.activity_reset_password.*

class ResetPasswordActivity : AppCompatActivity() {
    private companion object {
        const val TAG = "ResetPasswordActivity"

        const val INCORRECT_EMAIL_FORMAT_STATUS = 0
        const val SUCCESS_STATUS = 1
        const val FAIL_STATUS = 2
        const val CANCEL_STATUS = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        signInTextView.setOnClickListener { finish() }

        resetPasswordImageView.setOnClickListener {
            val email = resetPasswordEmailEditText.text.toString()

            if(!isEmailValid(email)) {
                val toast = Toast(this)
                toast.duration = Toast.LENGTH_LONG
                toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
                toast.view.findViewById<TextView>(R.id.toastTextView).text =
                    getString(R.string.sign_up_email_is_invalid)
                toast.show()

                return@setOnClickListener
            }
            resetPassword(email)
        }
    }

    private fun resetPassword(email: String) {
        run {
            val threeBounce = ThreeBounce()
            resetPasswordImageView.setImageDrawable(threeBounce)
            threeBounce.start()
            clickBlocker?.visibility = View.VISIBLE
        }

        if(!email.contains('@')) {
            showOnCompleteStatusToast(CANCEL_STATUS)
        }

        FirebaseAuth.getInstance()
            .sendPasswordResetEmail(email)
            .addOnCompleteListener {
                resetPasswordImageView.setImageDrawable(getDrawable(R.drawable.ic_continue_white))
                clickBlocker?.visibility = View.GONE
            }
            .addOnSuccessListener {
                Log.d(TAG, "Letter with instruction to reset password has been successfully sent to introduced email")

                showOnCompleteStatusToast(SUCCESS_STATUS)

                finish()
            }
            .addOnFailureListener {
                Log.d(TAG, "Fail while sending letter with instruction to reset password to introduced email")
                Log.d(TAG, "The cause is ${it.message}")

                showOnCompleteStatusToast(FAIL_STATUS)
            }
            .addOnCanceledListener {
                Log.d(TAG, "Current user does not have any permission to reset password operation")

                showOnCompleteStatusToast(CANCEL_STATUS)
            }
    }

    private fun showOnCompleteStatusToast(status: Int) {
        val toast = Toast(applicationContext)
        toast.view = layoutInflater.inflate(R.layout.layout_toast_custom, null, false)
        toast.duration = Toast.LENGTH_LONG
        when(status) {
            INCORRECT_EMAIL_FORMAT_STATUS -> toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.reset_password_incorrect_email_format)
            SUCCESS_STATUS -> toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.reset_password_successfully_sent_reset_password_letter)
            FAIL_STATUS -> toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.reset_password_failed_sent_reset_password_letter)
            CANCEL_STATUS -> toast.view.findViewById<TextView>(R.id.toastTextView).text = getString(R.string.reset_password_cancel_sent_reset_password_letter)
        }

        toast.show()
    }

    private fun isEmailValid(email: String): Boolean =  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}