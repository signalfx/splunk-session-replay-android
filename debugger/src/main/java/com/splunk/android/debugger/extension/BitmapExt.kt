package com.splunk.android.debugger.extension

import android.graphics.Bitmap
import com.splunk.android.common.utils.Colors

internal val Bitmap.aspectRatio: Float
    get() = width.toFloat() / height

internal fun Bitmap.setColors(colors: Colors) {
    setPixels(colors.colors, 0, colors.width, 0, 0, colors.width, colors.height)
}
