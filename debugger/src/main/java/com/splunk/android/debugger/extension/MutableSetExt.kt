package com.splunk.android.debugger.extension

internal operator fun MutableSet<Int>.plusAssign(array: IntArray) {
    for (item in array)
        add(item)
}
