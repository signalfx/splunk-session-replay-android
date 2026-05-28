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
