package com.splunk.android.instrumentation.recording.core.video

import com.splunk.android.common.logger.Logger
import com.splunk.android.instrumentation.recording.core.data.ApplicationFrame
import com.splunk.android.instrumentation.recording.core.data.VideoSize
import com.splunk.android.instrumentation.recording.core.display.DisplayHandler
import kotlin.math.min

internal object VideoFrameProcessingUtil {

    private const val TAG = "VideoFrameProcessingUtil"

    fun calculateVideoSize(applicationFrame: ApplicationFrame, maxVideoBiggerSize: Int): VideoSize {
        val maxVideoSize = maxVideoSize(applicationFrame, maxVideoBiggerSize)
        return optimizeForEncoder(maxVideoSize.width, maxVideoSize.height)
    }

    private fun normalizeVideoSize(maxVideoBiggerSize: Int): Int {
        val dummyUpperResolutionBound = DisplayHandler.longerDisplaySideSize() / 2
        return min(dummyUpperResolutionBound.toInt(), maxVideoBiggerSize)
    }

    private fun maxVideoSize(applicationFrame: ApplicationFrame, maxVideoBiggerSize: Int): VideoSize {
        val videoSize = if (applicationFrame.width > applicationFrame.height) {
            val normalizedBiggerSide = normalizeVideoSize(maxVideoBiggerSize)
            val normalizedSmallerSide = applicationFrame.height / applicationFrame.width.toFloat() * normalizedBiggerSide
            VideoSize(normalizedBiggerSide, normalizedSmallerSide.toInt())
        } else {
            val normalizedBiggerSide = normalizeVideoSize(maxVideoBiggerSize)
            val normalizedSmallerSide = applicationFrame.width / applicationFrame.height.toFloat() * normalizedBiggerSide
            VideoSize(normalizedSmallerSide.toInt(), normalizedBiggerSide)
        }

        return videoSize.also {
            Logger.i(TAG, "maxVideoSize() max video size calculated: videoSize = $it")
        }
    }

    private fun optimizeForEncoder(optimalWidth: Int, optimalHeight: Int): VideoSize {

        var optimalWidthForH264 = optimalWidth
        var optimalHeightForH264 = optimalHeight

        if (optimalWidthForH264 % 2 == 1) {
            optimalWidthForH264++
        }

        if (optimalHeightForH264 % 2 == 1) {
            optimalHeightForH264++
        }

        optimalWidthForH264 -= optimalWidthForH264 % 16
        optimalHeightForH264 -= optimalHeightForH264 % 16

        return VideoSize(optimalWidthForH264, optimalHeightForH264)
    }
}
