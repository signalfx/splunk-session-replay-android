package com.splunk.android.instrumentation.recording.core.api.handler

import com.splunk.android.instrumentation.recording.core.api.DataListener
import com.splunk.android.instrumentation.recording.core.api.RecordingMask

internal interface CoreApiHandler {

    val dataListeners: MutableCollection<DataListener>

    var recordingMask: RecordingMask?

    fun start()

    fun stop()

    fun newDataChunk()

    fun reset()
}
