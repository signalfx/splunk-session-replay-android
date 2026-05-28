package com.splunk.android.instrumentation.recording.core.api

import com.splunk.android.instrumentation.recording.core.api.handler.StateApiHandler

/**
 * @see SessionReplay.state
 */
class State internal constructor(
    private val api: StateApiHandler
) {

    /**
     * The current SDK status.
     */
    val status: Status
        get() = api.status

    /**
     * The current number frames per second.
     */
    val frameRate: Int
        get() = api.frameRate

    /**
     * Screen data rendering mode.
     */
    val renderingMode: RenderingMode
        get() = api.renderingMode

    /**
     * Recording bit rate
     */
    val recordingQuality: RecordingQuality
        get() = api.recordingQuality
}
