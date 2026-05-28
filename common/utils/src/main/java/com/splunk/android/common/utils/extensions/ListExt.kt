package com.splunk.android.common.utils.extensions

import android.view.View
import android.view.WindowManager

fun <T> List<Pair<View, T>>.sortedItemsByDecorViews(): MutableList<T> {
    val list = ArrayList<Pair<View, T>>(this)
    var isSorted: Boolean

    do {
        isSorted = true

        for (i in 0 until list.lastIndex) {
            val first = list[i]
            val firstView = first.first
            val second = list[i + 1]
            val secondView = second.first

            if (firstView.activity === secondView.activity) {
                val firstLayout = firstView.layoutParams as? WindowManager.LayoutParams ?: continue
                val secondLayout = secondView.layoutParams as? WindowManager.LayoutParams ?: continue

                if (firstLayout.type > secondLayout.type) {
                    list[i] = second
                    list[i + 1] = first
                    isSorted = false
                }
            }
        }
    } while (!isSorted)

    val sortedList = ArrayList<T>(size)
    list.forEachFast { sortedList += it.second }

    return sortedList
}

/**
 * Function is using indices instead of iterator.
 */
inline fun <T> List<T>.forEachFast(crossinline consumer: (T) -> Unit) {
    for (i in indices)
        consumer(get(i))
}

inline fun <T> List<T>.anyFast(crossinline predicate: (T) -> Boolean): Boolean {
    for (i in indices)
        if (!predicate(get(i)))
            return false

    return true
}

inline fun <T> List<T>.findFast(crossinline predicate: (T) -> Boolean): T? {
    for (i in indices) {
        val element = get(i)

        if (predicate(element))
            return element
    }

    return null
}

internal fun <T> List<T>.emitDifferences(list: List<T>, consumer: DifferencesConsumer<T>) {
    for (element in list)
        if (element !in this)
            consumer.onNew(element)

    for (element in this)
        if (element !in list)
            consumer.onMiss(element)
}

interface DifferencesConsumer<T> {
    fun onNew(element: T)
    fun onMiss(element: T)
}
