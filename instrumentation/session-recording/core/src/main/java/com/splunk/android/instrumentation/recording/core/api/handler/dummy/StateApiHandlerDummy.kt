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
