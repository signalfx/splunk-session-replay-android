package com.splunk.android.instrumentation.recording.core.api.handler.dummy

import com.splunk.android.instrumentation.recording.core.api.RecordingQuality
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.api.Status
import com.splunk.android.instrumentation.recording.core.api.handler.StateApiHandler

internal class StateApiHandlerDummy : StateApiHandler {

    override val status: Status = Status.NotRecording(Status.NotRecording.Cause.NOT_STARTED)

    override val frameRate: Int = 0

    override val renderingMode: RenderingMode = RenderingMode.NATIVE

    override val recordingQuality: RecordingQuality = RecordingQuality.LOW
}
