package com.splunk.android.instrumentation.recording.wireframe.extension

internal fun CharSequence.isBlank(startIndex: Int = 0, endIndex: Int = length): Boolean {
    for (i in startIndex until endIndex)
        if (!get(i).isWhitespace())
            return false

    return true
}

internal fun CharSequence.getFirstNonWhitespaceCharIndex(startIndex: Int = 0, endIndex: Int = length): Int? {
    for (i in startIndex until endIndex)
        if (!get(i).isWhitespace())
            return i

    return null
}

internal fun CharSequence.getLastNonWhitespaceCharIndex(startIndex: Int = 0, endIndex: Int = length): Int? {
    for (i in (startIndex until endIndex).reversed())
        if (!get(i).isWhitespace())
            return i + 1

    return null
}
