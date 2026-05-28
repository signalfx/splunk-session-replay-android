package com.splunk.android.instrumentation.recording.wireframe.model

import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats

/**
 * Single screen frame description with debug statistics.
 */
data class WireframeData internal constructor(
    val frame: Wireframe.Frame,
    val stats: WireframeStats
)
