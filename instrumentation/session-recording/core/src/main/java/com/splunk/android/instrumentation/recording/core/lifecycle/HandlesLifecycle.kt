package com.splunk.android.instrumentation.recording.core.lifecycle

internal interface HandlesLifecycle {
    fun registerLifecycleCallback(): LifecycleCallback
}
