package com.splunk.android.instrumentation.recording.core.api

import com.splunk.android.instrumentation.recording.core.api.handler.PreferencesApiHandler

class Preferences internal constructor(
    private val api: PreferencesApiHandler
) {
    /**
     * Preferred number of frames per second.
     */
    var frameRate: Int?
        get() = api.frameRate
        set(value) {
            api.frameRate = value
        }

    /**
     * Preferred screen data rendering mode.
     */
    var renderingMode: RenderingMode?
        get() = api.renderingMode
        set(value) {
            api.renderingMode = value
        }

    /**
     * Preferred recording bit rate
     */
    var recordingQuality: RecordingQuality?
        get() = api.recordingQuality
        set(value) {
            api.recordingQuality = value
        }
}
