package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.RectF

internal fun RectF.isInside(rect: RectF): Boolean {
    return left >= rect.left && top >= rect.top && right <= rect.right && bottom <= rect.bottom
}
