package com.splunk.android.instrumentation.recording.wireframe.extension

internal fun CharArray.getFirstNonWhitespaceCharIndex(index: Int = 0, count: Int = size): Int? {
    for (i in index until index + count)
        if (!get(i).isWhitespace())
            return i

    return null
}

@Suppress("UseWithIndex")
internal fun CharArray.getLastWhitespaceCharCount(index: Int = 0, count: Int = size): Int {
    val end = index + count
    var whitespaceCount = 0

    for (i in (index until end).reversed()) {
        if (!get(i).isWhitespace())
            return whitespaceCount

        whitespaceCount++
    }

    return count
}
