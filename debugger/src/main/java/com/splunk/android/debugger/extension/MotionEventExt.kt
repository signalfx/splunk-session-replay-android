package com.splunk.android.debugger.extension

import android.view.MotionEvent

internal fun MotionEvent.copy(x: Float, y: Float): MotionEvent {
    val pointerPropertiesArray = Array(pointerCount) { copyPointerProperties(it) }
    val pointerCoords = Array(pointerCount) { copyPointerCoords(it) }

    val firstPointerProperties = pointerPropertiesArray.first()
    firstPointerProperties.clear()
    firstPointerProperties.id = 0

    val firstPointerCoords = pointerCoords.first()
    firstPointerCoords.clear()
    firstPointerCoords.x = x
    firstPointerCoords.y = y
    firstPointerCoords.pressure = 1f
    firstPointerCoords.size = 1f

    return MotionEvent.obtain(downTime, eventTime, action, pointerCount, pointerPropertiesArray, pointerCoords, metaState, buttonState, xPrecision, yPrecision, deviceId, edgeFlags, source, flags)
}

internal fun MotionEvent.copyPointerProperties(pointerIndex: Int): MotionEvent.PointerProperties {
    val properties = MotionEvent.PointerProperties()
    getPointerProperties(pointerIndex, properties)
    return properties
}

internal fun MotionEvent.copyPointerCoords(pointerIndex: Int): MotionEvent.PointerCoords {
    val coords = MotionEvent.PointerCoords()
    getPointerCoords(pointerIndex, coords)
    return coords
}
