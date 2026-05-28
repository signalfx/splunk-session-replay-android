package com.splunk.android.instrumentation.recording.core.api

interface DataListener {
    fun onData(data: ByteArray, metadata: Metadata): Boolean
}
