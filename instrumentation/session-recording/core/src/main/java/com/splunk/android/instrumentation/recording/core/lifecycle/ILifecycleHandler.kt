package com.splunk.android.instrumentation.recording.core.lifecycle

import android.app.Application

internal interface ILifecycleHandler {
    val handlesLifecycleList: MutableList<HandlesLifecycle>

    fun setup(applicationContext: Application)

    fun applicationCrash(cause: Throwable)

    fun startRecording()

    fun stopRecording()
}
