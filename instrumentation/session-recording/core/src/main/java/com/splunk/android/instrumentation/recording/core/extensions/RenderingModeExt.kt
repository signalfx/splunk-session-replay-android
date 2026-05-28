package com.splunk.android.instrumentation.recording.core.extensions

import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.data.RenderingDataSource

internal fun RenderingMode.toFrameCapturerMode(): FrameCapturer.Mode {
    return when (this) {
        RenderingMode.NATIVE -> FrameCapturer.Mode.WIREFRAME_SCREENSHOT
        RenderingMode.WIREFRAME_ONLY -> FrameCapturer.Mode.WIREFRAME
    }
}

internal fun RenderingMode.toRenderingDataOption(): List<RenderingDataSource> {
    return when (this) {
        RenderingMode.NATIVE -> listOf(
            RenderingDataSource.WIREFRAME,
            RenderingDataSource.NATIVE
        )
        RenderingMode.WIREFRAME_ONLY -> listOf(
            RenderingDataSource.WIREFRAME
        )
    }
}
