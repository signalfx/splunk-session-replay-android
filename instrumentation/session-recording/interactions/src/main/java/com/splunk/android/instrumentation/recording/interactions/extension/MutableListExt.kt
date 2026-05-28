package com.splunk.android.instrumentation.recording.interactions.extension

internal fun <E> MutableList<E>.insertOrdered(item: E, reversed: Boolean, comparator: Comparator<E>) {
    if (isNotEmpty()) {
        val range = if (reversed) indices.reversed() else indices

        for (i in range)
            if (comparator.compare(get(i), item) <= 0) {
                add(i + 1, item)
                break
            }
    } else
        add(item)
}
