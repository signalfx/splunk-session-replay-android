package com.splunk.android.instrumentation.recording.interactions.extension

import android.content.Context
import android.os.Build
import android.view.Display
import com.splunk.android.common.utils.extensions.windowManager
import com.splunk.android.common.utils.runOnAndroidAtLeast

@get:Suppress("DEPRECATION")
internal val Context.displayCompat: Display
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.R) { display } ?: windowManager.defaultDisplay

internal val Context.isAssociatedWithDisplayCompat: Boolean
    get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            try {
                // ContextImpl.getDisplay() calls ContextImpl.isAssociatedWithDisplay() and throws exception when false
                display
            } catch (_: UnsupportedOperationException) {
                return false
            }

        return true
    }
