package com.splunk.android.instrumentation.recording.screenshot.model

import android.graphics.Bitmap

class Screenshot internal constructor(
    val time: Long,
    val bitmap: Bitmap
) {

    override fun toString(): String {
        return "Screenshot(time=$time, bitmap.width=${bitmap.width}, bitmap.height=${bitmap.height})"
    }

    companion object
}
