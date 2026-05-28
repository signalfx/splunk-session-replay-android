package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Matrix

// Keep in mind, this is not thread safe

private val values = FloatArray(9)

internal val Matrix.translationX: Float
    get() = getValues()[2]

internal val Matrix.translationY: Float
    get() = getValues()[5]

internal val Matrix.scaleX: Float
    get() = getValues()[0]

internal val Matrix.scaleY: Float
    get() = getValues()[4]

private fun Matrix.getValues(): FloatArray {
    getValues(values)
    return values
}
