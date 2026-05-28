package com.splunk.android.debugger.extension

import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

internal fun Wireframe.Frame.Scene.Window.View.Skeleton.Color.Radii.toFloatArray(): FloatArray {
    return floatArrayOf(topLeft.toFloat(), topLeft.toFloat(), topRight.toFloat(), topRight.toFloat(), bottomRight.toFloat(), bottomRight.toFloat(), bottomLeft.toFloat(), bottomLeft.toFloat())
}
