package com.splunk.android.instrumentation.recording.wireframe.extension

import android.content.Context
import android.view.Surface
import android.view.WindowManager
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Orientation

@Suppress("DEPRECATION")
internal val Context.orientation: Orientation
    get() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> Orientation.PORTRAIT
            Surface.ROTATION_90 -> Orientation.LANDSCAPE
            Surface.ROTATION_180 -> Orientation.PORTRAIT_REVERSED
            Surface.ROTATION_270 -> Orientation.LANDSCAPE_REVERSED
            else -> Orientation.PORTRAIT
        }
    }
