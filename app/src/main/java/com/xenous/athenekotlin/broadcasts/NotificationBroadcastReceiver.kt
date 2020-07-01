package com.xenous.athenekotlin.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "AtheneNotify"
        const val NOTIFICATION_ID_KEY = "AtheneCheckWordsNotify"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        TODO("Not yet implemented")
    }
}