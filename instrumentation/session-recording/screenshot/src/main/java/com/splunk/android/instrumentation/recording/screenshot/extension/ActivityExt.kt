package com.splunk.android.instrumentation.recording.screenshot.extension

import android.app.Activity
import com.splunk.android.common.utils.extensions.contentView

var Activity.isInvisibleForScreenshot: Boolean
    get() = contentView?.isInvisibleForScreenshot ?: error("Must be called after setContentView()")
    set(value) {
        val contentView = contentView ?: error("Must be called after setContentView()")
        contentView.isInvisibleForScreenshot = value
    }
