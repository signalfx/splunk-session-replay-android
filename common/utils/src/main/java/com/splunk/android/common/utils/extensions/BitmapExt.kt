package com.splunk.android.common.utils.extensions

import android.graphics.Bitmap
import android.graphics.Bitmap.Config

operator fun Bitmap.get(x: Int, y: Int): Int {
    return getPixel(x, y)
}

fun Bitmap.copyOrNull(config: Config = this.config ?: Config.ARGB_8888, isMutable: Boolean = true): Bitmap? {
    return try {
        copy(config, isMutable)
    } catch (_: OutOfMemoryError) {
        null
    }
}
