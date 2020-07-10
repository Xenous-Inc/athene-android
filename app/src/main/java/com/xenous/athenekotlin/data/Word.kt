package com.xenous.athenekotlin.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.xenous.athenekotlin.utils.forbiddenSymbols
import com.xenous.athenekotlin.utils.getCurrentDateTimeInMills
import java.util.*

data class Word(
    var native: String,
    var foreign: String,
    var category: String = "Без категории",
    var lastDateCheck: Long = 0,
    var level: Long = 0,
    var uid: String? = null
) : Parcelable {
    companion object  CREATOR : Parcelable.Creator<Word> {
        override fun createFromParcel(parcel: Parcel): Word {
            return Word(parcel)
        }

        override fun newArray(size: Int): Array<Word?> {
            return arrayOfNulls(size)
        }

        const val LEVEL_ADDED = -2
        const val LEVEL_ARCHIVED = -1
        const val LEVEL_DAY = 0
        const val LEVEL_WEEK = 1
        const val LEVEL_MONTH = 2
        const val LEVEL_QUARTER = 3
        const val LEVEL_HALF = 4
        const val LEVEL_YEAR = 5
        val CHECK_INTERVAL = hashMapOf(
            LEVEL_DAY to 86400000L,
            LEVEL_WEEK to 604800000L,
            LEVEL_MONTH to 2592000000L,
            LEVEL_QUARTER to 7776000000L,
            LEVEL_HALF to 15552000000L,
            LEVEL_YEAR to 31104000000L
        )

        const val WORD_IS_NULL = -1
        const val WORD_IS_OK = 0
        const val WORD_CONTAINS_FORBIDDEN_SYMBOLS = 1
        const val WORD_IS_TO_LONG = 2
    }

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()
    )

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "Russian" to native,
            "English" to foreign,
            "category" to category,
            "date" to lastDateCheck,
            "level" to level
        )
    }

    fun filter(): Int {
        if(native == null && foreign == null) {
            return WORD_IS_NULL
        }

        val equawalent = listOf(native, foreign)

        for(element in equawalent) {
            if(element.length >= 35) {
                return WORD_IS_TO_LONG
            }

            for(forbiddenSymbol in forbiddenSymbols) {
                if(element.contains(forbiddenSymbol)) {
                    return WORD_CONTAINS_FORBIDDEN_SYMBOLS
                }
            }
        }

        return WORD_IS_OK
    }

    fun increaseLevel() {
        if(level == LEVEL_ARCHIVED.toLong()) {
            return
        }

        level += 1
        setNextDate()
    }

    fun resetProgress() {
        level = 0
        setNextDate()
    }

    private fun setNextDate() {
        if(level == LEVEL_ARCHIVED.toLong()) {
            return
        }
        lastDateCheck = getCurrentDateTimeInMills() + CHECK_INTERVAL[level.toInt()]!!
    }

    override fun equals(other: Any?): Boolean {
        if(other is Word) {
            if(
                this.foreign.trim().toLowerCase(Locale.ROOT) == other.foreign.trim().toLowerCase(Locale.ROOT) &&
                this.native.trim().toLowerCase(Locale.ROOT) == other.native.trim().toLowerCase(Locale.ROOT)
            ) {
                return true
            }
        }

        return false
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(native)
        parcel.writeString(foreign)
        parcel.writeString(category)
        parcel.writeLong(lastDateCheck)
        parcel.writeLong(level)
        parcel.writeString(uid)
    }

    override fun describeContents(): Int {
        return 0
    }
}