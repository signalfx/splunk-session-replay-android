/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
