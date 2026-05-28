package com.splunk.android.debugger.extension

internal fun <T> List<T>.none(startIndex: Int, predicate: (T) -> Boolean): Boolean {
    for (j in startIndex until size)
        if (predicate(get(j)))
            return false

    return true
}
