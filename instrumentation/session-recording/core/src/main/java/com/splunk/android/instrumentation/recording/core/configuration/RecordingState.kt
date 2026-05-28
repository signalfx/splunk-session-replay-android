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
