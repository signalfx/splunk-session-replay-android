package com.splunk.android.instrumentation.recording.screenshot.extension

import java.util.LinkedList

internal fun <E> LinkedList<E>.removeFirst(predicate: (E) -> Boolean): E? {
    return iterator().removeOnce(predicate)
}

internal fun <E> LinkedList<E>.removeLast(predicate: (E) -> Boolean): E? {
    return descendingIterator().removeOnce(predicate)
}
