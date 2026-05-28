package com.splunk.android.instrumentation.recording.core.storage.extension

val Int.MB: Long
    get() = (this * 1024 * 1024).toLong()
