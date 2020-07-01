package com.xenous.athenekotlin.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.xenous.athenekotlin.broadcasts.NotificationBroadcastReceiver


fun isEmailValid(email: String): Boolean =  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

const val SUCCESS_CODE = 9000
const val ERROR_CODE = 9001

const val USER_REFERENCE = "users"
const val WORDS_REFERENCE = "words"
const val CATEGORY_REFERENCE = "categories"

const val NATIVE_WORD_DATABASE_KEY = "English"
const val LEARNING_WORD_DATABASE_KEY = "Russian"
const val WORD_CATEGORY_DATABASE_KEY = "category"
const val WORD_LAST_DATE_DATABASE_KEY = "date"
const val WORD_LEVEL_DATABASE_KEY = "level"

@RequiresApi(api = Build.VERSION_CODES.O)
fun createNotificationChannel(c: Context) {
    val name: CharSequence = "AtheneReminderChannel"
    val description = "Notification channel for Athene Reminder"
    val importance = NotificationManager.IMPORTANCE_HIGH
    val channel =
        NotificationChannel(NotificationBroadcastReceiver.NOTIFICATION_ID_KEY, name, importance)

    channel.enableLights(true)
    channel.enableVibration(true)
    channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    channel.description = description
    val notificationManager: NotificationManager =
        c.getSystemService(NotificationManager::class.java)!!

    notificationManager.createNotificationChannel(channel)
}