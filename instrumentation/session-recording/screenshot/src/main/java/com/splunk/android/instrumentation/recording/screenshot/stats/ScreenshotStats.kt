package com.splunk.android.instrumentation.recording.screenshot.stats

class ScreenshotStats internal constructor(
    val totalTime: Float,
    val copyTime: Float,
    val windowCopyTime: Float,
    val surfaceCopyTime: Float,
    val finalDrawTime: Float,
    val windowCount: Int,
    val surfaceCount: Int,
    val sensitivityTime: Float
) {

    val othersTime: Float = totalTime - copyTime - sensitivityTime - finalDrawTime

    override fun toString(): String {
        return "ScreenshotStats(totalTime=$totalTime, copyTime=$copyTime, windowCopyTime=$windowCopyTime, surfaceCopyTime=$surfaceCopyTime, finalDrawTime=$finalDrawTime, windowCount=$windowCount, surfaceCount=$surfaceCount, sensitivityTime=$sensitivityTime, othersTime=$othersTime)"
    }

    companion object
}
