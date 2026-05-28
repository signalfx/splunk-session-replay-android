package com.splunk.android.common.encoder.extensions

import android.graphics.Bitmap
import android.graphics.Matrix

internal fun Bitmap.rotated(angle: Int): Bitmap {
    return if (angle % 360 != 0) {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())

        val bitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        recycle()
        bitmap
    } else
        this
}
