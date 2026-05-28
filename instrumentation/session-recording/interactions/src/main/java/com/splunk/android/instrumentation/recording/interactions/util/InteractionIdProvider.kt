package com.splunk.android.instrumentation.recording.interactions.util

internal object InteractionIdProvider {

    private var id = 0

    fun next(): Int {
        return id++
    }
}
