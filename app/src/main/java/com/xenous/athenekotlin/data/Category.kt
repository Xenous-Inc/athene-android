package com.xenous.athenekotlin.data

data class Category(
    val title: String,
    val uid: String? = null
) {

    companion object {
        const val NOT_THE_MATCHES = 0
        const val IS_A_URL = 1
        const val IS_A_NUMBER = 2
    }
}