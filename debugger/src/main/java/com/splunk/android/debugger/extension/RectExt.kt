package com.splunk.android.debugger.extension

import android.graphics.Rect

internal val Rect.aspectRatio: Float
    get() = width().toFloat() / height()
