package com.splunk.android.instrumentation.recording.interactions.extension

import android.app.Activity
import com.splunk.android.common.utils.extensions.contentView

var Activity.isInvisibleForInteractions: Boolean
    get() = contentView?.rootView?.isInvisibleForInteractions ?: error("Must be called after setContentView()")
    set(value) {
        val rootView = contentView?.rootView ?: error("Must be called after setContentView()")
        rootView.isInvisibleForInteractions = value
    }
