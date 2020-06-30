package com.xenous.athenekotlin.utils

fun isEmailValid(email: String): Boolean =  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()