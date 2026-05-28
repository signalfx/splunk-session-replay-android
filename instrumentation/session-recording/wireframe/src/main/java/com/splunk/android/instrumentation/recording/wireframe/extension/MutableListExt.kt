package com.splunk.android.instrumentation.recording.wireframe.extension

internal fun <E> MutableList<E>.set(items: List<E>) {
    loop@ for (i in indices.reversed()) {
        val item = get(i)

        for (newItem in items)
            if (item == newItem)
                continue@loop

        removeAt(i)
    }

    loop@ for (i in items.indices) {
        val newItem = items[i]

        for (item in this)
            if (item == newItem)
                continue@loop

        add(newItem)
    }
}
