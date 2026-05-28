package com.splunk.android.instrumentation.recording.core.display

import android.app.Activity

internal interface IDisplayHandler {
    fun setupRealDisplaySize(activity: Activity)

    fun longerDisplaySideSize(): Float

    fun displayWidthPx(): Float

    fun displayHeightPx(): Float

    fun getScreenDensity(): Float
}
