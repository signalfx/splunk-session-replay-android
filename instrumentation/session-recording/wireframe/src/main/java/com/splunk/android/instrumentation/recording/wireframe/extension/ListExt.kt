package com.splunk.android.instrumentation.recording.wireframe.extension

import com.splunk.android.common.utils.extensions.plusAssign

internal operator fun <E> List<E>?.plus(element: E): List<E> {
    return when {
        this == null ->
            listOf(element)
        else -> {
            val newList = ArrayList(this)
            newList += element
            newList
        }
    }
}

internal operator fun <E> List<E>?.plus(list: List<E>): List<E> {
    val newList = ArrayList<E>()
    newList += this
    newList += list
    return newList
}

internal fun <E, R> List<E>.findLastNotNullValue(predicate: (E) -> R?): R? {
    for (element in asReversed())
        return predicate(element) ?: continue

    return null
}
