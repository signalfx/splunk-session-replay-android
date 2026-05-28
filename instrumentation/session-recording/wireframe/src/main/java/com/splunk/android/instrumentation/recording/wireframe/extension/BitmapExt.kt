package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Bitmap

internal fun Bitmap.toPrettyString(): String {
    return "Bitmap(width: $width, height: $height, config: $config)"
}
