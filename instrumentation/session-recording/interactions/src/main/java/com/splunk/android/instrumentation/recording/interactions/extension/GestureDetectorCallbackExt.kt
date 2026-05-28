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

package com.splunk.android.instrumentation.recording.interactions.extension

import com.splunk.android.instrumentation.recording.interactions.gesture.GestureDetector
import com.splunk.android.instrumentation.recording.interactions.model.Interaction

internal fun GestureDetector.Callback.SwipeDirection.toDirection(): Interaction.Touch.Gesture.Swipe.Direction {
    return when (this) {
        GestureDetector.Callback.SwipeDirection.LEFT -> Interaction.Touch.Gesture.Swipe.Direction.LEFT
        GestureDetector.Callback.SwipeDirection.RIGHT -> Interaction.Touch.Gesture.Swipe.Direction.RIGHT
        GestureDetector.Callback.SwipeDirection.UP -> Interaction.Touch.Gesture.Swipe.Direction.UP
        GestureDetector.Callback.SwipeDirection.DOWN -> Interaction.Touch.Gesture.Swipe.Direction.DOWN
    }
}

internal fun GestureDetector.Callback.PointerType.toPointerType(): Interaction.Touch.Pointer.Type {
    return when (this) {
        GestureDetector.Callback.PointerType.FINGER -> Interaction.Touch.Pointer.Type.FINGER
        GestureDetector.Callback.PointerType.STYLUS -> Interaction.Touch.Pointer.Type.STYLUS
        GestureDetector.Callback.PointerType.MOUSE -> Interaction.Touch.Pointer.Type.MOUSE
        GestureDetector.Callback.PointerType.ERASER -> Interaction.Touch.Pointer.Type.ERASER
        else -> Interaction.Touch.Pointer.Type.UNKNOWN
    }
}
