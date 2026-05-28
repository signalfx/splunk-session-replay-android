package com.splunk.android.instrumentation.recording.interactions.extension

import android.graphics.Point
import android.os.Build
import android.view.Display
import androidx.annotation.RequiresApi

@get:Suppress("DEPRECATION")
@get:RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal val Display.realSize: Point
    get() {
        val point = Point()
        getRealSize(point)
        return point
    }
