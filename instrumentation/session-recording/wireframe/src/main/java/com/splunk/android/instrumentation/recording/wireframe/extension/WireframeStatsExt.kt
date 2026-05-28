package com.splunk.android.instrumentation.recording.wireframe.extension

import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats

fun WireframeStats.Companion.createEmpty(): WireframeStats {
    return WireframeStats(0f, 1, 0f, 1, 0f, 0, 0f, 0, 0)
}
