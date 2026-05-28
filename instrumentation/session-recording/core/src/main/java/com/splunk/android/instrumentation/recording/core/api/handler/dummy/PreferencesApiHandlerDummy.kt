package com.splunk.android.instrumentation.recording.core.api.handler.dummy

import com.splunk.android.instrumentation.recording.core.api.RecordingQuality
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.api.handler.PreferencesApiHandler

internal class PreferencesApiHandlerDummy : PreferencesApiHandler {

    override var frameRate: Int? = null

    override var renderingMode: RenderingMode? = null

    override var recordingQuality: RecordingQuality? = null
}
