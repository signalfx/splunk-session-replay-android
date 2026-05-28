package com.splunk.android.instrumentation.recording.screenshot.extension

import android.view.SurfaceView
import com.splunk.android.common.utils.extensions.get
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.screenshot.R

// MARK Requires Proguard rules

private val flutterSurfaceViewClass = "io.flutter.embedding.android.FlutterSurfaceView".toClass()

internal val SurfaceView.isDrawnOnTop: Boolean
    get() {
        var value = getTag(R.id.sl_tag_drawn_on_top) as? Boolean

        if (value != null)
            return value

        value = when (this::class.java) {
            flutterSurfaceViewClass ->
                runCatching { get<Boolean>("renderTransparently") }.getOrNull() ?: false
            else ->
                false // FIXME Investigate setZOrderOnTop
        }

        setTag(R.id.sl_tag_drawn_on_top, value)
        return value
    }
