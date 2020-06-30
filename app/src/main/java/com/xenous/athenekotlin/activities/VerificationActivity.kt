package com.xenous.athenekotlin.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xenous.athenekotlin.R
import kotlinx.android.synthetic.main.activity_verification.*

class VerificationActivity : AppCompatActivity() {
    companion object {
        const val TAG = "VerificationActivity"
        const val COUNTDOWN_DURATION_IN_SECONDS = 60
        const val VERIFY_SUCCESS = 9073
        const val VERIFY_PROCESSING = 9074
        const val COUNTDOWN_TICK = 9075
        const val COUNTDOWN_END = 9076
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

//        Calling async recursive function
//        That waiting for email verification
//        And calls onEmailVerifiedHandler on success
        waitForEmailVerify(getOnEmailVerifiedHandler())

        val countdownTextView = findViewById<TextView>(R.id.countDownTextView)
        countdownTextView.setOnClickListener {
            countdownTextView.isClickable = false

            Firebase.auth.currentUser?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    setCountdownTimer(getCountdownHandler(countdownTextView))

                    if(task.isSuccessful) {
                        Toast
                            .makeText(
                                this,
                                getString(R.string.verification_successfully_sent_verification_letter),
                                Toast.LENGTH_LONG
                            ).show()
                    }
                    else {
//                        todo: handle error
                        Log.d(SignUpActivity.TAG, "Failed to send verification letter. ${task.exception}")
                        Toast
                            .makeText(
                                this,
                                getString(R.string.verification_failed_to_send_verification_letter),
                                Toast.LENGTH_LONG
                            ).show()
                    }
                }
        }
    }

    private fun waitForEmailVerify(handler: Handler) {
//        Getting cached user
        val currentCachedUser = Firebase.auth.currentUser
//        Reloading it from Internet
        currentCachedUser?.reload()?.addOnSuccessListener {
            val currentUser = Firebase.auth.currentUser
//            Checking it on nullability and email verification
//            Then sending message to handler
            if(currentUser != null && currentUser.isEmailVerified) {
                val msg = Message.obtain()
                msg.apply {
                    what = VERIFY_SUCCESS
                    obj = currentUser
                }
                handler.sendMessage(msg)
            }
            else {
//                Send PROCESSING message to handler
//                And call function again
                handler.sendEmptyMessage(VERIFY_PROCESSING)
                waitForEmailVerify(handler)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private fun getOnEmailVerifiedHandler() = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
//            If email verified, start main activity
            if(msg.what == VERIFY_SUCCESS && msg.obj is FirebaseUser) {
                Log.d(TAG, "Successfully verified mail,")
                val intent = Intent(this@VerificationActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
//            Just for logs
            if(msg.what == VERIFY_PROCESSING) {
                Log.d(TAG, "Waiting for email verify...")
            }
        }
    }

//    Setting countdown timer for SendMessage button
    private fun setCountdownTimer(handler: Handler) {
        var countdown = COUNTDOWN_DURATION_IN_SECONDS

        Thread {
            do {
                val msg = Message.obtain()
                msg.apply {
                    what = COUNTDOWN_TICK
                    obj = countdown
                }
                handler.sendMessage(msg)

                Thread.sleep(1000)
            } while(--countdown >= 0)

            handler.sendEmptyMessage(COUNTDOWN_END)
        }.start()
    }

    @SuppressLint("HandlerLeak")
    private fun getCountdownHandler(countdownTextView: TextView) = object : Handler() {
        @SuppressLint("ResourceAsColor")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            var countdownString = getString(R.string.verification_send_again)
            if(msg.what == COUNTDOWN_TICK && msg.obj is Int) {
                Log.d(TAG, "Countdown tick: ${msg.obj}")
                countdownString += " (${msg.obj})"
                val color = ContextCompat.getColor(this@VerificationActivity, R.color.colorTextShadowed)
                countdownTextView.setTextColor(color)
                countDownTextView.setBackgroundWithoutPaddingBreak(R.drawable.button_outline_background_shadowed)
                countdownTextView.isClickable = false
            }
            else if(msg.what == COUNTDOWN_END) {
                val color = ContextCompat.getColor(this@VerificationActivity, R.color.colorTextSecondary)
                countdownTextView.setTextColor(color)
                countDownTextView.setBackgroundWithoutPaddingBreak(R.drawable.button_outline_background_secondary)
                countdownTextView.isClickable = true
            }

            countdownTextView.text = countdownString
        }
    }

//    When changing the background, some indents may fly away, so we use this method
    private fun View.setBackgroundWithoutPaddingBreak(drawableId: Int) {
        val drawable =
            ContextCompat.getDrawable(this@VerificationActivity, drawableId)
        val pS = this.paddingStart
        val pT = this.paddingTop
        val pE = this.paddingEnd
        val pB = this.paddingBottom
        this.background = drawable
        this.setPadding(pS, pT, pE, pB)
    }
}