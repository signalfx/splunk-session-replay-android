package com.splunk.android.instrumentation.recording.core

import android.os.Build

internal object Constants {
    // Min sdk android for which we support recording.
    const val MIN_ANDROID_SDK = Build.VERSION_CODES.LOLLIPOP

    // FrameRate
    const val DEFAULT_FRAMERATE = 2
    const val MIN_FRAMERATE = 1
    const val MAX_FRAMERATE = 10

    // Bitrate
    const val LOW_BITRATE = 80_000L
    const val MEDIUM_BITRATE = 160_000L
    const val HIGH_BITRATE = 320_000L

    // Video size
    const val DEFAULT_MAX_VIDEO_HEIGHT = 720 // px

    const val MAX_RECORD_LENGTH = 20_000L // in ms
}
