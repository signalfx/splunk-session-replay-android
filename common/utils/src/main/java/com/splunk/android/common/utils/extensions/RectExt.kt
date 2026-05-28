package com.splunk.android.common.utils.extensions

import android.graphics.Rect
import android.graphics.RectF

fun Rect.toRectF(): RectF {
    return RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
}
