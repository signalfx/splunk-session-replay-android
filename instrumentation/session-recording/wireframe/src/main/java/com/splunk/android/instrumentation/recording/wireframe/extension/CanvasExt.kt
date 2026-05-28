package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Canvas

internal fun Canvas.translate(x: Int, y: Int) {
    translate(x.toFloat(), y.toFloat())
}
