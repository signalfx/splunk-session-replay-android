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

package com.splunk.android.instrumentation.recording.core.configuration

import com.splunk.android.instrumentation.recording.core.api.Status

/**
 * Represents the state of recording. If the recording is Allowed or Not.
 */
internal sealed interface RecordingState {

    object Allowed : RecordingState

    data class NotAllowed(val cause: Cause) : RecordingState {
        enum class Cause {
            NOT_ENOUGH_STORAGE_SPACE,
            MISSING_CODEC,
        }
    }
}

internal fun RecordingState.toStatus(): Status {
    return when (this) {
        RecordingState.Allowed -> Status.Recording
        is RecordingState.NotAllowed -> {
            when (this.cause) {
                RecordingState.NotAllowed.Cause.NOT_ENOUGH_STORAGE_SPACE -> Status.NotRecording(
                    cause = Status.NotRecording.Cause.STORAGE_LIMIT_REACHED
                )
                RecordingState.NotAllowed.Cause.MISSING_CODEC -> Status.NotRecording(cause = Status.NotRecording.Cause.INTERNAL_ERROR)
            }
        }
    }
}
