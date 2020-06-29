package com.xenous.athenekotlin.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xenous.athenekotlin.R

class VerificationActivity : AppCompatActivity() {
    companion object {
        const val TAG = "VerificationActivity"
        const val VERIFY_SUCCESS = 9073
        const val VERIFY_PROCESSING = 9074
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

//        Calling async recursive function
//        That waiting for email verification
//        And calls onEmailVerifiedHandler on success
        waitForEmailVerify(getOnEmailVerifiedHandler())
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
}