package com.splunk.android.instrumentation.recording.wireframe.extension

import android.app.Activity
import com.splunk.android.common.utils.extensions.contentView

var Activity.isInvisibleForWireframe: Boolean
    get() = contentView?.isInvisibleForWireframe ?: error("Must be called after setContentView()")
    set(value) {
        val contentView = contentView ?: error("Must be called after setContentView()")
        contentView.isInvisibleForWireframe = value
    }
