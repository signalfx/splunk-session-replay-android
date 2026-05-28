package com.splunk.android.instrumentation.recording.screenshot.extension

import android.graphics.Bitmap
import android.graphics.Rect
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot

fun Screenshot.Companion.createEmpty(rect: Rect, time: Long): Screenshot {
    val bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888)
    bitmap.eraseColor(0xff7C8697.toInt())

    return Screenshot(
        time = time,
        bitmap = bitmap
    )
}
