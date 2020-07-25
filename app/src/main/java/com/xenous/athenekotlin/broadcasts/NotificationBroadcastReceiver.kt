package com.xenous.athenekotlin.broadcasts

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.xenous.athenekotlin.R
import com.xenous.athenekotlin.activities.MainActivity
import com.xenous.athenekotlin.utils.startAlarm


class NotificationBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val TAG = "AtheneNotify"
        const val NOTIFICATION_ID_KEY = "AtheneCheckWordsNotify"
        const val NOTIFICATION_ID = 332
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context != null) {
            startAlarm(context)
        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Call Notification
        val builder =
            NotificationCompat.Builder(context!!, NOTIFICATION_ID_KEY)
                .setSmallIcon(R.drawable.ic_notification)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText(context.getString(R.string.notification_content_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManagerCompat =
            NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
    }
}