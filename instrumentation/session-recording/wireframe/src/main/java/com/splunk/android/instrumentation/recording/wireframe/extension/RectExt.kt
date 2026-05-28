package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.round

internal fun Rect.set(rect: RectF) {
    set(rect.left, rect.top, rect.right, rect.bottom)
}

internal fun Rect.set(left: Float, top: Float, right: Float, bottom: Float) {
    set((left + 0.5f).toInt(), (top + 0.5f).toInt(), (right + 0.5f).toInt(), (bottom + 0.5f).toInt())
}

internal fun Rect.copy(offsetX: Int, offsetY: Int): Rect {
    val rect = Rect(this)
    rect.offset(offsetX, offsetY)
    return rect
}

internal fun Rect.scale(scaleX: Float, scaleY: Float) {
    val sx = abs(scaleX)
    val sy = abs(scaleY)

    if (sx == 1f && sy == 1f)
        return

    left = round(left * sx).toInt()
    top = round(top * sy).toInt()
    right = round(right * sx).toInt()
    bottom = round(bottom * sy).toInt()
}

internal fun Rect.scale(scaleX: Float, scaleY: Float, pivotX: Int, pivotY: Int) {
    val sx = abs(scaleX)
    val sy = abs(scaleY)

    if (sx == 1f && sy == 1f)
        return

    if (pivotX != 0 || pivotY != 0) {
        left = round((left - pivotX) * sx + pivotX).toInt()
        top = round((top - pivotY) * sy + pivotY).toInt()
        right = round((right - pivotX) * sx + pivotX).toInt()
        bottom = round((bottom - pivotY) * sy + pivotY).toInt()
    } else
        scale(sx, sy)
}

internal fun Rect.isInside(rect: Rect): Boolean {
    return left >= rect.left && top >= rect.top && right <= rect.right && bottom <= rect.bottom
}
