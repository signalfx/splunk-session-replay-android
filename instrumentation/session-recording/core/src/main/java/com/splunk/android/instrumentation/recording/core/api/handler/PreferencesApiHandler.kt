package com.splunk.android.instrumentation.recording.core.api.handler

import com.splunk.android.instrumentation.recording.core.api.RecordingQuality
import com.splunk.android.instrumentation.recording.core.api.RenderingMode

internal interface PreferencesApiHandler {

    var frameRate: Int?

    var renderingMode: RenderingMode?

    var recordingQuality: RecordingQuality?
}
