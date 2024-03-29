package com.xenous.athenekotlin.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.xenous.athenekotlin.broadcasts.NotificationBroadcastReceiver
import com.xenous.athenekotlin.data.Word
import java.util.*


fun isEmailValid(email: String): Boolean =  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

const val SUCCESS_CODE = 9000
const val ERROR_CODE = 9001

const val USER_REFERENCE = "user"
const val USERS_REFERENCE = "users"
const val WORDS_REFERENCE = "words"
const val CATEGORY_REFERENCE = "categories"

const val STUDENTS_REFERENCE = "students"
const val TEACHER_REFERENCE = "teacher"
const val TEACHERS_REFERENCE = "teachers"
const val TEACHER_NAME_DATABASE_KEY = "name"
const val CLASSROOM_REFERENCE = "class"
const val CLASSROOMS_DATABASE_KEY = "classes"
const val CLASSROOM_NAME_DATABASE_KEY = "name"
const val CLASSROOM_CATEGORIES_REFERENCE = "categories"
const val STUDENT_NAME_DATABASE_KEY = "name"
const val STUDENT_KEY_DATABASE_KEY = "id"

const val CLASSROOM_CATEGORY_NAME_DATABASE_KEY = "name"

const val NATIVE_WORD_DATABASE_KEY = "Russian"
const val FOREIGN_WORD_DATABASE_KEY = "English"
const val CLASSROOM_NATIVE_WORD_DATABASE_KEY = "Russian"
const val CLASSROOM_FOREIGN_WORD_DATABASE_KEY = "English"
const val WORD_CATEGORY_DATABASE_KEY = "category"
const val WORD_LAST_DATE_DATABASE_KEY = "date"
const val WORD_LEVEL_DATABASE_KEY = "level"

val forbiddenSymbols = listOf('@', '_', ';', ':', '*', '%', '#', '"', '[', ']')

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

fun startAlarm(context: Context) {
    val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent: PendingIntent
    val calendar: Calendar = GregorianCalendar()

    calendar.set(Calendar.HOUR_OF_DAY, 12)
    calendar.set(Calendar.MINUTE, 0)

    val intent = Intent(context, NotificationBroadcastReceiver::class.java)
    pendingIntent =
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

    manager.cancel(pendingIntent)
    manager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis + Word.CHECK_INTERVAL[Word.LEVEL_DAY]!!, pendingIntent)
}

fun getCurrentDateTimeAtZeroHoursInMills(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.timeInMillis
}