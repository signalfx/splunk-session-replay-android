package com.splunk.android.instrumentation.recording.core.video

import com.splunk.android.instrumentation.recording.core.lifecycle.HandlesLifecycle

internal interface IScreenCapturer : HandlesLifecycle {
    fun start()
    fun stop()

    fun newDataChunk()
}
