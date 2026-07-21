/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
