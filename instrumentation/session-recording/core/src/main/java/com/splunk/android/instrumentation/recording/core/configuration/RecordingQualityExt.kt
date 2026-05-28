package com.splunk.android.instrumentation.recording.core.configuration

import com.splunk.android.instrumentation.recording.core.Constants
import com.splunk.android.instrumentation.recording.core.api.RecordingQuality

internal fun RecordingQuality.toBitRate(): Long = when (this) {
    RecordingQuality.LOW -> Constants.LOW_BITRATE
    RecordingQuality.MEDIUM -> Constants.MEDIUM_BITRATE
    RecordingQuality.HIGH -> Constants.HIGH_BITRATE
}
