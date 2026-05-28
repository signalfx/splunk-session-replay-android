package com.splunk.android.instrumentation.recording.core.api.handler.impl

import com.splunk.android.instrumentation.recording.core.Core
import com.splunk.android.instrumentation.recording.core.api.RecordingQuality
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.api.Status
import com.splunk.android.instrumentation.recording.core.api.handler.StateApiHandler
import com.splunk.android.instrumentation.recording.core.configuration.IConfigurationHandler

internal class StateApiHandlerImpl(
    private val configurationHandler: IConfigurationHandler,
    private val core: Core
) : StateApiHandler {

    override val status: Status
        get() = core.status

    override val frameRate: Int
        get() {
            return configurationHandler.frameRate
        }

    override val renderingMode: RenderingMode
        get() {
            return configurationHandler.renderingMode
        }

    override val recordingQuality: RecordingQuality
        get() {
            return configurationHandler.recordingQuality
        }
}
