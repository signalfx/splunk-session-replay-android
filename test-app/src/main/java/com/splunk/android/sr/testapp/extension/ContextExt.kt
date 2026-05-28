package com.splunk.android.sr.testapp.extension

import android.content.Context
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager

val Context.screenSize: Size
    get() {
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(metrics)
        return Size(metrics.widthPixels, metrics.heightPixels)
    }
