package com.splunk.android.instrumentation.recording.core.api.handler.impl

import com.splunk.android.instrumentation.recording.core.Core
import com.splunk.android.instrumentation.recording.core.api.DataListener
import com.splunk.android.instrumentation.recording.core.api.RecordingMask
import com.splunk.android.instrumentation.recording.core.api.handler.CoreApiHandler

internal class CoreApiHandlerImpl(
    private val core: Core,
) : CoreApiHandler {
    override val dataListeners: MutableCollection<DataListener> = HashSet()

    override var recordingMask: RecordingMask? = null

    override fun start() {
        core.start()
    }

    override fun stop() {
        core.stop()
    }

    override fun newDataChunk() {
        core.newDataChunk()
    }

    override fun reset() {
        core.reset()
    }
}
