package com.splunk.android.common.utils.extensions

/**
 * Function is using indices instead of iterator if possible.
 */
inline fun <T> Collection<T>.forEachFast(crossinline consumer: (T) -> Unit) {
    if (this is List)
        forEachFast(consumer)
    else
        forEach(consumer)
}

inline fun <T> Collection<T>.findFast(crossinline predicate: (T) -> Boolean): T? {
    if (this is List)
        for (i in indices) {
            val item = get(i)

            if (predicate(item))
                return item
        }
    else
        for (element in this)
            if (predicate(element))
                return element

    return null
}

inline fun <T> Collection<T>.noneFast(predicate: (T) -> Boolean): Boolean {
    if (isEmpty())
        return true

    if (this is List) {
        for (i in indices)
            if (predicate(get(i)))
                return false
    } else
        for (element in this)
            if (predicate(element))
                return false

    return true
}

operator fun <T> Collection<T>.contains(elements: Collection<T>): Boolean {
    return containsAll(elements)
}

inline fun <T : Any, reified R : T> Collection<T>.findInstance(): R? {
    return find { it is R } as? R
}
