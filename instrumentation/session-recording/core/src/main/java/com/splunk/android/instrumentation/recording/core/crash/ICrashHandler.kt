package com.splunk.android.instrumentation.recording.core.crash

internal interface ICrashHandler {
    fun register()
    fun unregister()
}
