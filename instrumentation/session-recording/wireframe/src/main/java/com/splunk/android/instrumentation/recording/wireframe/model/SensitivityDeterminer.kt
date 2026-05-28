package com.splunk.android.instrumentation.recording.wireframe.model

import android.view.View

interface SensitivityDeterminer {
    fun isViewSensitive(view: View): Boolean?
}
