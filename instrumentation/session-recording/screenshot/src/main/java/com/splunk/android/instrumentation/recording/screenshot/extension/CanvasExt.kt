package com.splunk.android.instrumentation.recording.screenshot.extension

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal fun Canvas.drawRoundRect(rect: Rect, rx: Float, ry: Float, paint: Paint) {
    drawRoundRect(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), rx, ry, paint)
}
