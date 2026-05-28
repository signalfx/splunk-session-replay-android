package com.splunk.android.instrumentation.recording.screenshot.extension

import com.splunk.android.instrumentation.recording.screenshot.stats.ScreenshotStats

fun ScreenshotStats.Companion.createEmpty(): ScreenshotStats {
    return ScreenshotStats(0f, 0f, 0f, 0f, 0f, 0, 0, 0f)
}
