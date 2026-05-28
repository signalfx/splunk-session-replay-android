package com.splunk.android.instrumentation.recording.interactions.extension

internal fun <T> Collection<T>.mapToIntArray(transform: (T) -> Int): IntArray {
    val array = IntArray(size)
    var index = 0

    for (value in this)
        array[index++] = transform(value)

    return array
}
