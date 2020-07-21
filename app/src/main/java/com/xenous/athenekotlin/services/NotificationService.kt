package com.xenous.athenekotlin.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.xenous.athenekotlin.broadcasts.NotificationBroadcastReceiver
import com.xenous.athenekotlin.data.Word
import com.xenous.athenekotlin.utils.createNotificationChannel
import com.xenous.athenekotlin.utils.startAlarm
import java.util.*


class NotificationService : Service() {

    private companion object {
        const val TAG = "NotificationService"
    }

    override fun onCreate() {
        super.onCreate()

        Log.w(TAG, "Set up alarm to tomorrow")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(this);
            startAlarm(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}