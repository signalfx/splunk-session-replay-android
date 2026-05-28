package com.splunk.android.instrumentation.recording.capturer

import android.graphics.Rect

interface ScreenMasksProvider {
    fun onScreenMasksRequested(): List<Rect>
}
