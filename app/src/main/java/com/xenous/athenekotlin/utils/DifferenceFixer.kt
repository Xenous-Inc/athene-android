package com.xenous.athenekotlin.utils

import com.xenous.athenekotlin.data.Category

class DifferenceFixer(private val original: ArrayList<Category>, private val other: ArrayList<Category>) {
    data class Difference(val type: Type, val index: Int, val element: Category) {
        enum class Type(val value: Int){
            ADDED_TO(9193), REMOVED_AT(9194)
        }

        override fun toString(): String {
            super.toString()

            var string = "An element $element "
            string += when(type) {
                Type.ADDED_TO -> "was added to index $index"
                Type.REMOVED_AT -> "was removed at index $index"
            }

            return string
        }
    }

    private var indexOfLastRemovedElement = 0

    fun getNextDifference(): Difference? {
        var indexOfLastSavedElement = 0

        for(index in 0 until other.size) {
            if(original.contains(other[index])) {
                indexOfLastSavedElement = original.indexOf(other[index])
            }
            else {
                original.add(indexOfLastSavedElement+1, other[index])
                return Difference(Difference.Type.ADDED_TO, indexOfLastSavedElement+1, other[index])
            }
        }

        for(index in indexOfLastRemovedElement until original.size) {
            if(!other.contains(original[index])) {
                return Difference(
                    Difference.Type.REMOVED_AT,
                    indexOfLastRemovedElement++,
                    original[index]
                )
            }

            indexOfLastRemovedElement++
        }

        return null
    }
}