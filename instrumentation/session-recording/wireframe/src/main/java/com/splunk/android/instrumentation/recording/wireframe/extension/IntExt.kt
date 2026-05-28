package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Color

fun Int.mulAlpha(alpha: Int): Int {
    val newAlpha = alpha / 255f * Color.alpha(this) / 255f
    return withAlpha(newAlpha)
}

internal fun Int.withAlpha(alpha: Int): Int {
    return (this and 0x00ffffff) or (alpha shl 24)
}

internal fun Int.withAlpha(alpha: Float): Int {
    return (this and 0x00ffffff) or ((alpha * 0xff).toInt() shl 24)
}

internal fun Int.isTransparent(): Boolean {
    return Color.alpha(this) == 0x00
}
