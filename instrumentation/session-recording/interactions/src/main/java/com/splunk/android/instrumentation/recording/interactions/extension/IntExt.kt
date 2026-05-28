package com.splunk.android.instrumentation.recording.interactions.extension

internal fun Int.toIntArray(): IntArray {
    return IntArray(1) { this }
}
