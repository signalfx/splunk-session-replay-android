package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

private val bounds = RectF()

internal fun Path.toBitmap(): Bitmap? {
    if (isEmpty)
        return null

    val rect = RectF()
    computeBounds(rect, true)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = Color.WHITE

    val bitmap = Bitmap.createBitmap(rect.right.toInt(), rect.bottom.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawPath(this, paint)

    return bitmap
}

internal fun Path.toPrettyString(): String {
    computeBounds(bounds, false)
    return "Path(bounds: $bounds)"
}
