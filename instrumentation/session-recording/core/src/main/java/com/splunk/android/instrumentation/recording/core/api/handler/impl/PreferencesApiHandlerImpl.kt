package com.splunk.android.instrumentation.recording.core.api.handler.impl

import com.splunk.android.instrumentation.recording.core.Constants
import com.splunk.android.instrumentation.recording.core.api.RecordingQuality
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.api.handler.PreferencesApiHandler
import com.splunk.android.instrumentation.recording.core.configuration.IConfigurationHandler

internal class PreferencesApiHandlerImpl(
    private val configurationHandler: IConfigurationHandler,
) : PreferencesApiHandler {

    override var frameRate: Int?
        get() {
            return configurationHandler.frameRate
        }
        set(value) {
            configurationHandler.frameRate = value ?: Constants.DEFAULT_FRAMERATE
        }

    override var renderingMode: RenderingMode?
        get() {
            return configurationHandler.renderingMode
        }
        set(value) {
            configurationHandler.renderingMode = value ?: RenderingMode.NATIVE
        }

    override var recordingQuality: RecordingQuality?
        get() {
            return configurationHandler.recordingQuality
        }
        set(value) {
            configurationHandler.recordingQuality = value ?: RecordingQuality.LOW
        }
}
