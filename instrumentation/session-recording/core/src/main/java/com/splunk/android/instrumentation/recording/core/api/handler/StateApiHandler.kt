package com.splunk.android.instrumentation.recording.core.api.handler

import com.splunk.android.instrumentation.recording.core.api.RecordingQuality
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.api.Status

internal interface StateApiHandler {

    val status: Status

    val frameRate: Int

    val renderingMode: RenderingMode

    val recordingQuality: RecordingQuality
}
