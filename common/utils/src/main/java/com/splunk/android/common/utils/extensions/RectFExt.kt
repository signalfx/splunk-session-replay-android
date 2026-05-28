package com.splunk.android.common.utils.extensions

import android.graphics.Rect
import android.graphics.RectF

fun RectF.toRect(): Rect {
    return Rect((left + 0.5f).toInt(), (top + 0.5f).toInt(), (right + 0.5f).toInt(), (bottom + 0.5f).toInt())
}
