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

package com.splunk.android.instrumentation.recording.interactions.model

import android.graphics.Rect

@Suppress("ArrayInDataClass")
sealed interface Interaction {

    val id: Int
    val timestamp: Long

    interface Targetable {
        val targetElementPath: List<ElementNode>?
    }

    data class PhoneButton(override val id: Int, override val timestamp: Long, val name: Name) : Interaction {
        enum class Name { BACK, VOLUME_DOWN, VOLUME_UP }
    }

    sealed interface Touch : Interaction, Targetable {

        override val targetElementPath: List<ElementNode>?

        interface Continuous {
            val isLast: Boolean
        }

        interface Focusable {
            val focusX: Int
            val focusY: Int
        }

        data class Pointer(override val id: Int, override val timestamp: Long, val pointerId: Int, val x: Int, val y: Int, val type: Type, val isHovering: Boolean, override val targetElementPath: List<ElementNode>?, override val isLast: Boolean) : Touch, Continuous {
            enum class Type { FINGER, MOUSE, STYLUS, ERASER, UNKNOWN }
        }

        sealed interface Gesture : Touch {

            val pointerIds: IntArray

            data class Tap(override val id: Int, override val timestamp: Long, override val pointerIds: IntArray, override val targetElementPath: List<ElementNode>?) : Gesture

            data class LongPress(override val id: Int, override val timestamp: Long, override val pointerIds: IntArray, override val targetElementPath: List<ElementNode>?) : Gesture

            data class DoubleTap(override val id: Int, override val timestamp: Long, override val pointerIds: IntArray, override val targetElementPath: List<ElementNode>?) : Gesture

            data class RageTap(override val id: Int, override val timestamp: Long, override val pointerIds: IntArray, override val targetElementPath: List<ElementNode>?) : Gesture

            data class Swipe(override val id: Int, override val timestamp: Long, override val pointerIds: IntArray, override val targetElementPath: List<ElementNode>?, val direction: Direction) : Gesture {
                enum class Direction { LEFT, RIGHT, UP, DOWN }
            }

            data class Pinch(override val id: Int, override val timestamp: Long, override val pointerIds: IntArray, override val targetElementPath: List<ElementNode>?, override val focusX: Int, override val focusY: Int, val distance: Int, override val isLast: Boolean) : Gesture, Focusable, Continuous

            data class Rotation(override val id: Int, override val timestamp: Long, override val pointerIds: IntArray, override val targetElementPath: List<ElementNode>?, override val focusX: Int, override val focusY: Int, val angle: Float, override val isLast: Boolean) : Gesture, Focusable, Continuous
        }
    }

    data class Keyboard(override val id: Int, override val timestamp: Long, val rect: Rect?) : Interaction {
        val isOpened: Boolean
            get() = rect != null

        val isClosed: Boolean
            get() = rect == null
    }

    data class Focus(override val id: Int, override val timestamp: Long, override val targetElementPath: List<ElementNode>?) : Interaction, Targetable

    data class Orientation(override val id: Int, override val timestamp: Long, val orientation: Orientation) : Interaction {
        enum class Orientation { PORTRAIT, LANDSCAPE }
    }

    companion object
}
