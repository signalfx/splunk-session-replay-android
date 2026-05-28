package com.splunk.android.debugger.extension

internal fun <T> MutableList<T>.removeLast(n: Int) {
    for (i in 0 until n)
        removeLast()
}
