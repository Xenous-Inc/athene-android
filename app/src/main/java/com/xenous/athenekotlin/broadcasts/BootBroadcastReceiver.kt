package com.xenous.athenekotlin.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.xenous.athenekotlin.services.NotificationService




class BootBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val serviceIntent = Intent(context, NotificationService::class.java)
        context!!.startService(serviceIntent)
    }
}