package com.splunk.android.instrumentation.recording.screenshot.extension

internal fun <E> MutableIterator<E>.removeOnce(predicate: (E) -> Boolean): E? {
    while (hasNext()) {
        val item = next()

        if (predicate(item)) {
            remove()
            return item
        }
    }

    return null
}
