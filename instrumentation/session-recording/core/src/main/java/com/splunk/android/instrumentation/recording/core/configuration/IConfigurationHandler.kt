package com.splunk.android.instrumentation.recording.core.configuration

import com.splunk.android.instrumentation.recording.core.api.RecordingQuality
import com.splunk.android.instrumentation.recording.core.api.RenderingMode

internal interface IConfigurationHandler {

    val listeners: MutableCollection<Listener>

    fun recordingState(): RecordingState

    var renderingMode: RenderingMode
    var frameRate: Int
    var recordingQuality: RecordingQuality
}

internal interface Listener {
    fun onRenderingModeChanged(mode: RenderingMode) {}
}
