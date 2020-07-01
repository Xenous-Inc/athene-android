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
            startAlarm();
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startAlarm() {
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent: PendingIntent
        val calendar: Calendar = GregorianCalendar()

        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)

        val intent = Intent(this, NotificationBroadcastReceiver::class.java)
        pendingIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        manager.cancel(pendingIntent)
        manager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + Word.CHECK_INTERVAL[Word.LEVEL_DAY]!!, pendingIntent)
    }
}