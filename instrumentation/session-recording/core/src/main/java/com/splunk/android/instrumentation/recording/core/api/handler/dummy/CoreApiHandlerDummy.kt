package com.splunk.android.instrumentation.recording.core.api.handler.dummy

import com.splunk.android.instrumentation.recording.core.api.DataListener
import com.splunk.android.instrumentation.recording.core.api.RecordingMask
import com.splunk.android.instrumentation.recording.core.api.handler.CoreApiHandler

internal class CoreApiHandlerDummy : CoreApiHandler {

    override val dataListeners: MutableCollection<DataListener> = HashSet()

    override var recordingMask: RecordingMask? = null

    override fun start() {}

    override fun stop() {}

    override fun newDataChunk() {}

    override fun reset() {}
}
