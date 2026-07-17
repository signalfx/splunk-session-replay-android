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

import android.graphics.Rect
import com.splunk.rum.common.utils.dpToPx
import com.splunk.rum.common.utils.extensions.plusAssign
import com.splunk.android.instrumentation.recording.interactions.model.ElementNode
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import kotlin.math.abs
import org.json.JSONArray
import org.json.JSONObject

// Sampling

private val moveThresholdSqr = dpToPx(50f).let { it * it }
private val pinchDistanceThreshold = dpToPx(20f)
private const val rotationAngleThreshold = 5f

/**
 * Samples interactions.
 * @param startTime interactions with timestamp lower than this value will be ignored
 * @param endTime interactions with timestamp higher than this value will be ignored
 * @param filteredTypes if not empty, only interactions of these types will be returned, otherwise all interactions will be returned
 */
fun List<Interaction>.sampled(startTime: Long = 0L, endTime: Long = Long.MAX_VALUE, vararg filteredTypes: Class<out Interaction>): List<Interaction> {
    val result = ArrayList<Interaction>()

    val activePointers = HashMap<Int, Interaction.Touch.Pointer>()
    var lastRotation: Interaction.Touch.Gesture.Rotation? = null
    var lastPinch: Interaction.Touch.Gesture.Pinch? = null
    var isTimeWindowEntered = false

    for (interaction in this)
        if (interaction.timestamp >= startTime) {
            if (interaction.timestamp > endTime)
                break

            if (!isTimeWindowEntered) {
                if (filteredTypes.isEmpty() || Interaction.Touch.Gesture.Rotation::class.java in filteredTypes)
                    result += lastRotation?.copy(timestamp = startTime)

                if (filteredTypes.isEmpty() || Interaction.Touch.Gesture.Pinch::class.java in filteredTypes)
                    result += lastPinch?.copy(timestamp = startTime)

                if (filteredTypes.isEmpty() || Interaction.Touch.Pointer::class.java in filteredTypes)
                    result += activePointers.values.map { it.copy(timestamp = startTime) }

                isTimeWindowEntered = true
                activePointers.clear()
                lastRotation = null
                lastPinch = null
            }

            if (filteredTypes.isNotEmpty() && interaction::class.java !in filteredTypes)
                continue

            if (interaction !is Interaction.Touch.Continuous || interaction.isLast) {
                when (interaction) {
                    is Interaction.Touch.Gesture.Pinch ->
                        lastPinch = null
                    is Interaction.Touch.Gesture.Rotation ->
                        lastRotation = null
                    is Interaction.Touch.Pointer ->
                        activePointers -= interaction.pointerId
                    else ->
                        Unit
                }

                result += interaction
            } else
                when (interaction) {
                    is Interaction.Touch.Gesture.Pinch -> {
                        if (lastPinch == null || abs(calcDistanceSqr(interaction.focusX, interaction.focusY, lastPinch.focusX, lastPinch.focusY)) >= moveThresholdSqr || abs(interaction.distance - lastPinch.distance) > pinchDistanceThreshold) {
                            lastPinch = interaction
                            result += interaction
                        }
                    }
                    is Interaction.Touch.Gesture.Rotation -> {
                        if (lastRotation == null || abs(calcDistanceSqr(interaction.focusX, interaction.focusY, lastRotation.focusX, lastRotation.focusY)) >= moveThresholdSqr || abs(interaction.angle - lastRotation.angle) > rotationAngleThreshold) {
                            lastRotation = interaction
                            result += interaction
                        }
                    }
                    is Interaction.Touch.Pointer -> {
                        val lastPointer = activePointers[interaction.pointerId]

                        if (lastPointer == null || abs(calcDistanceSqr(interaction.x, interaction.y, lastPointer.x, lastPointer.y)) >= moveThresholdSqr) {
                            activePointers[interaction.pointerId] = interaction
                            result += interaction
                        }
                    }
                    else ->
                        throw IllegalStateException()
                }
        } else if (interaction is Interaction.Touch.Continuous)
            when (interaction) {
                is Interaction.Touch.Gesture.Pinch -> {
                    lastPinch = if (interaction.isLast) null else interaction
                }
                is Interaction.Touch.Gesture.Rotation -> {
                    lastRotation = if (interaction.isLast) null else interaction
                }
                is Interaction.Touch.Pointer -> {
                    if (interaction.isLast)
                        activePointers.remove(interaction.pointerId)
                    else
                        activePointers[interaction.pointerId] = interaction
                }
                else ->
                    throw IllegalArgumentException("Missing interaction")
            }

    return result
}

private fun calcDistanceSqr(x1: Int, y1: Int, x2: Int, y2: Int): Int {
    val x = x1 - x2
    val y = y1 - y2
    return x * x + y * y
}

// JSON Serialize

fun List<Interaction>.toJSONArray(startTime: Long = 0L): JSONArray {
    val array = JSONArray()

    for (value in this)
        array.put(value.toJSONObject(startTime))

    return array
}

private fun Interaction.toJSONObject(startTime: Long): JSONObject {
    val json = JSONObject()
        .put("id", id.toString())
        .put("time", timestamp - startTime)

    return when (this) {
        is Interaction.PhoneButton -> {
            json.put("type", "phoneButton")
            json.put("phoneButton", toJSONObject())
        }
        is Interaction.Touch.Gesture.DoubleTap -> {
            json.put("type", "gestureDoubleTap")
            json.put("gestureDoubleTap", toJSONObject())
        }
        is Interaction.Touch.Gesture.LongPress -> {
            json.put("type", "gestureLongPress")
            json.put("gestureLongPress", toJSONObject())
        }
        is Interaction.Touch.Gesture.Pinch -> {
            json.put("type", "gesturePinch")
            json.put("gesturePinch", toJSONObject())
        }
        is Interaction.Touch.Gesture.RageTap -> {
            json.put("type", "gestureRageTap")
            json.put("gestureRageTap", toJSONObject())
        }
        is Interaction.Touch.Gesture.Rotation -> {
            json.put("type", "gestureRotation")
            json.put("gestureRotation", toJSONObject())
        }
        is Interaction.Touch.Gesture.Swipe -> {
            json.put("type", "gestureSwipe")
            json.put("gestureSwipe", toJSONObject())
        }
        is Interaction.Touch.Gesture.Tap -> {
            json.put("type", "gestureTap")
            json.put("gestureTap", toJSONObject())
        }
        is Interaction.Touch.Pointer -> {
            json.put("type", "pointer")
            json.put("pointer", toJSONObject())
        }
        is Interaction.Keyboard -> {
            json.put("type", "softKeyboard")
            json.put("softKeyboard", toJSONObject())
        }
        is Interaction.Focus -> {
            json.put("type", "focus")
            json.put("focus", toJSONObject())
        }
        is Interaction.Orientation -> {
            json.put("type", "orientation")
            json.put("orientation", orientation.toPayload())
        }
    }
}

private fun Interaction.Focus.toJSONObject(): JSONObject {
    return JSONObject()
        .put("targetElementId", targetElementPath?.formatId() ?: JSONObject.NULL)
}

private fun Interaction.Keyboard.toJSONObject(): JSONObject {
    return JSONObject()
        .put("rect", rect?.toJSONObject() ?: JSONObject.NULL)
}

private fun Rect.toJSONObject(): JSONObject {
    return JSONObject()
        .put("x", left)
        .put("y", top)
        .put("width", width())
        .put("height", height())
}

private fun Interaction.PhoneButton.toJSONObject(): JSONObject {
    return JSONObject()
        .put("name", name.toPayload())
}

private fun Interaction.Touch.Gesture.DoubleTap.toJSONObject(): JSONObject {
    return JSONObject()
        .put("targetElementId", targetElementPath?.formatId())
        .put("pointerIds", pointerIds.toJSONArray())
}

private fun Interaction.PhoneButton.Name.toPayload(): String {
    return when (this) {
        Interaction.PhoneButton.Name.BACK -> "back"
        Interaction.PhoneButton.Name.VOLUME_DOWN -> "volumeDown"
        Interaction.PhoneButton.Name.VOLUME_UP -> "volumeUp"
    }
}

private fun String.toPhoneButtonName(): Interaction.PhoneButton.Name {
    return when (this) {
        "back" -> Interaction.PhoneButton.Name.BACK
        "volumeDown" -> Interaction.PhoneButton.Name.VOLUME_DOWN
        "volumeUp" -> Interaction.PhoneButton.Name.VOLUME_UP
        else -> throw IllegalArgumentException("Unknown PhoneButton.Name of name '$this'")
    }
}

private fun Interaction.Touch.Gesture.LongPress.toJSONObject(): JSONObject {
    return JSONObject()
        .put("targetElementId", targetElementPath?.formatId())
        .put("pointerIds", pointerIds.toJSONArray())
}

private fun Interaction.Touch.Gesture.Pinch.toJSONObject(): JSONObject {
    return JSONObject()
        .put("targetElementId", targetElementPath?.formatId())
        .put("pointerIds", pointerIds.toJSONArray())
        .put("focusX", focusX)
        .put("focusY", focusY)
        .put("distance", distance)
        .put("isLast", if (isLast) true else null)
}

private fun Interaction.Touch.Gesture.Tap.toJSONObject(): JSONObject {
    return JSONObject()
        .put("targetElementId", targetElementPath?.formatId())
        .put("pointerIds", pointerIds.toJSONArray())
}

private fun Interaction.Touch.Gesture.RageTap.toJSONObject(): JSONObject {
    return JSONObject()
        .put("targetElementId", targetElementPath?.formatId())
        .put("pointerIds", pointerIds.toJSONArray())
}

private fun Interaction.Touch.Pointer.toJSONObject(): JSONObject {
    return JSONObject()
        .put("id", pointerId)
        .put("x", x)
        .put("y", y)
        .put("type", if (type == Interaction.Touch.Pointer.Type.FINGER) null else type.toPayload())
        .put("isLast", if (isLast) true else null)
        .put("isHovering", if (isHovering) true else null)
}

private fun Interaction.Touch.Pointer.Type.toPayload(): String {
    return when (this) {
        Interaction.Touch.Pointer.Type.FINGER -> "finger"
        Interaction.Touch.Pointer.Type.MOUSE -> "mouse"
        Interaction.Touch.Pointer.Type.STYLUS -> "stylus"
        Interaction.Touch.Pointer.Type.ERASER -> "eraser"
        Interaction.Touch.Pointer.Type.UNKNOWN -> "finger"
    }
}

private fun String.toPointerType(): Interaction.Touch.Pointer.Type {
    return when (this) {
        "finger" -> Interaction.Touch.Pointer.Type.FINGER
        "mouse" -> Interaction.Touch.Pointer.Type.MOUSE
        "stylus" -> Interaction.Touch.Pointer.Type.STYLUS
        "eraser" -> Interaction.Touch.Pointer.Type.ERASER
        else -> throw IllegalArgumentException("Unknown Swipe.Direction of name '$this'")
    }
}

private fun Interaction.Touch.Gesture.Rotation.toJSONObject(): JSONObject {
    return JSONObject()
        .put("targetElementId", targetElementPath?.formatId())
        .put("pointerIds", pointerIds.toJSONArray())
        .put("focusX", focusX)
        .put("focusY", focusY)
        .put("angle", angle)
        .put("isLast", if (isLast) true else null)
}

private fun Interaction.Touch.Gesture.Swipe.toJSONObject(): JSONObject {
    return JSONObject()
        .put("targetElementId", targetElementPath?.formatId())
        .put("pointerIds", pointerIds.toJSONArray())
        .put("direction", direction.toPayload())
}

private fun Interaction.Touch.Gesture.Swipe.Direction.toPayload(): String {
    return when (this) {
        Interaction.Touch.Gesture.Swipe.Direction.LEFT -> "left"
        Interaction.Touch.Gesture.Swipe.Direction.RIGHT -> "right"
        Interaction.Touch.Gesture.Swipe.Direction.UP -> "up"
        Interaction.Touch.Gesture.Swipe.Direction.DOWN -> "down"
    }
}

private fun String.toSwipeDirection(): Interaction.Touch.Gesture.Swipe.Direction {
    return when (this) {
        "left" -> Interaction.Touch.Gesture.Swipe.Direction.LEFT
        "right" -> Interaction.Touch.Gesture.Swipe.Direction.RIGHT
        "up" -> Interaction.Touch.Gesture.Swipe.Direction.UP
        "down" -> Interaction.Touch.Gesture.Swipe.Direction.DOWN
        else -> throw IllegalArgumentException("Unknown Swipe.Direction of name '$this'")
    }
}

private fun List<ElementNode>.formatId(): String {
    return joinToString("/") { if (it.positionInList != null) "${it.view.id}#${it.positionInList}" else it.view.id }
}

private fun Interaction.Orientation.Orientation.toPayload(): String {
    return when (this) {
        Interaction.Orientation.Orientation.PORTRAIT -> "portrait"
        Interaction.Orientation.Orientation.LANDSCAPE -> "landscape"
    }
}

// JSON deserialize

/*fun Interaction.Companion.fromJSONObject(json: JSONObject): Interaction {
    val id = json.getString("id").toInt()
    val time = json.getLong("time")
    val type = json.getString("type")
    val data = json.getJSONObject(type)

    return when (type) {
        "phoneButton" ->
            Interaction.PhoneButton(
                id = id,
                timestamp = time,
                name = data.getString("name").toPhoneButtonName()
            )
        "gestureDoubleTap" ->
            Interaction.Touch.Gesture.DoubleTap(
                id = id,
                timestamp = time,
                pointerIds = data.getJSONArray("pointerIds").toIntArray(),
                targetElementPath = data.optStringNull("targetElementId")
            )
        "gestureLongPress" ->
            Interaction.Touch.Gesture.LongPress(
                id = id,
                timestamp = time,
                pointerIds = data.getJSONArray("pointerIds").toIntArray(),
                targetElementId = data.optStringNull("targetElementId")
            )
        "gesturePinch" ->
            Interaction.Touch.Gesture.Pinch(
                id = id,
                timestamp = time,
                pointerIds = data.getJSONArray("pointerIds").toIntArray(),
                targetElementId = data.optStringNull("targetElementId"),
                focusX = data.getInt("focusX"),
                focusY = data.getInt("focusY"),
                distance = data.getInt("distance"),
                isLast = data.optBoolean("isLast", false)
            )
        "gestureRageTap" ->
            Interaction.Touch.Gesture.RageTap(
                id = id,
                timestamp = time,
                pointerIds = data.getJSONArray("pointerIds").toIntArray(),
                targetElementId = data.optStringNull("targetElementId")
            )
        "gestureRotation" ->
            Interaction.Touch.Gesture.Rotation(
                id = id,
                timestamp = time,
                pointerIds = data.getJSONArray("pointerIds").toIntArray(),
                targetElementId = data.optStringNull("targetElementId"),
                focusX = data.getInt("focusX"),
                focusY = data.getInt("focusY"),
                angle = data.getFloat("angle"),
                isLast = data.optBoolean("isLast", false)
            )
        "gestureSwipe" ->
            Interaction.Touch.Gesture.Swipe(
                id = id,
                timestamp = time,
                pointerIds = data.getJSONArray("pointerIds").toIntArray(),
                targetElementId = data.optStringNull("targetElementId"),
                direction = data.getString("direction").toSwipeDirection()
            )
        "gestureTap" ->
            Interaction.Touch.Gesture.Tap(
                id = id,
                timestamp = time,
                pointerIds = data.getJSONArray("pointerIds").toIntArray(),
                targetElementId = data.optStringNull("targetElementId")
            )
        "pointer" ->
            Interaction.Touch.Pointer(
                id = id,
                timestamp = time,
                pointerId = data.getInt("id"),
                x = data.getInt("x"),
                y = data.getInt("y"),
                type = data.optStringNull("type")?.toPointerType() ?: Interaction.Touch.Pointer.Type.FINGER,
                isHovering = data.optBoolean("isHovering", false),
                targetElementId = data.optStringNull("targetElementId"),
                isLast = data.optBoolean("isLast", false)
            )
        "softKeyboard" ->
            Interaction.Keyboard(
                id = id,
                timestamp = time,
                rect = data.optJSONObject("rect")?.let {
                    val x = it.getInt("x")
                    val y = it.getInt("y")
                    val width = it.getInt("width")
                    val height = it.getInt("height")

                    Rect(x, y, x + width, y + height)
                }
            )
        "focus" ->
            Interaction.Focus(
                id = id,
                timestamp = time,
                targetElementId = data.optStringNull("targetElementId"),
                targetType = data.optStringNull("targetType")?.toFocusTargetType()
            )
        else ->
            throw IllegalArgumentException("Unknown Interaction type of name '$type'")
    }
}*/
