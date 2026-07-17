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

package com.splunk.android.instrumentation.recording.interactions.gesture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.splunk.android.instrumentation.recording.interactions.extension.actionX
import com.splunk.android.instrumentation.recording.interactions.extension.actionY
import com.splunk.android.instrumentation.recording.interactions.extension.isTouchUp
import com.splunk.android.instrumentation.recording.interactions.extension.mapToIntArray
import com.splunk.android.instrumentation.recording.interactions.extension.pointerId
import com.splunk.android.instrumentation.recording.interactions.model.ElementNodeInfo
import com.splunk.android.instrumentation.recording.interactions.model.LegacyData
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

internal class GestureDetector(context: Context, private val callback: Callback) {

    private val config = ViewConfiguration.get(context)
    private val handler = LongTapHandler()

    private val activePointers = HashMap<Int, Pointer>()

    private var isPotentialTap = false
    private var isPotentialLongPress = false
    private var isPotentialMultiTap = false
    private var potentialTapDownTimestamp = 0L
    private var potentialTapDownX = 0
    private var potentialTapDownY = 0
    private var potentialMultiTapPreviousTapUpTimestamp = 0L
    private val potentialMultiTapPointerIds = ArrayList<Int>()

    private var lastRotationData: RotationData? = null
    private var lastPinchData: PinchData? = null

    fun onTouchEvent(event: MotionEvent, targetElementPath: List<ElementNodeInfo>?, legacyData: LegacyData) {
        val timestamp = System.currentTimeMillis()
        val diffX = event.rawX - event.x
        val diffY = event.rawY - event.y

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                val pointer = updateOrCreatePointer(event, diffX, diffY)
                callback.onPointer(timestamp, pointer.id, pointer.x, pointer.y, pointer.type, false, targetElementPath, false)

                if (event.pointerCount == 1) {
                    isPotentialMultiTap = timestamp - potentialMultiTapPreviousTapUpTimestamp < ViewConfiguration.getDoubleTapTimeout() && isDistanceValidForTap(event, diffX, diffY)
                    isPotentialLongPress = true
                    isPotentialTap = true

                    potentialTapDownTimestamp = timestamp
                    potentialTapDownX = pointer.x
                    potentialTapDownY = pointer.y

                    if (!isPotentialMultiTap)
                        potentialMultiTapPointerIds.clear()

                    handler.waitForLongPress(event.pointerId, targetElementPath)
                } else {
                    isPotentialLongPress = false
                    isPotentialMultiTap = false
                    isPotentialTap = false

                    handler.cancelLongPress()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                updateAndReportPointerMove(timestamp, event, diffX, diffY, targetElementPath)

                if (event.pointerCount == 1) {
                    if (isPotentialTap || isPotentialLongPress) {
                        val isDistanceValid = isDistanceValidForTap(event, diffX, diffY)

                        val elapsed = timestamp - potentialTapDownTimestamp
                        val isStillPotentialTap = elapsed <= ViewConfiguration.getTapTimeout()
                        val isStillPotentialLongPress = elapsed <= ViewConfiguration.getLongPressTimeout()

                        if (!isStillPotentialTap || !isDistanceValid) {
                            isPotentialTap = false
                            handler.cancelTap()
                        }

                        if (!isStillPotentialLongPress || !isDistanceValid) {
                            isPotentialLongPress = false
                            handler.cancelLongPress()
                        }
                    }
                } else {
                    val slPointerIds = activePointers.values.mapToIntArray { it.id }

                    val rotationData = calcRotation(event, lastRotationData, diffX, diffY)
                    rotationData?.let { reportRotation(timestamp, it, slPointerIds, targetElementPath, false) }
                    lastRotationData = rotationData

                    val pinchData = calcPinch(event, lastPinchData, diffX, diffY)
                    pinchData?.let { reportPinch(timestamp, it, slPointerIds, targetElementPath, false) }
                    lastPinchData = pinchData
                }
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                val slPointerIds = activePointers.values.mapToIntArray { it.id }
                val pointer = activePointers.remove(event.pointerId) ?: return
                val x = (event.actionX + diffX).toInt()
                val y = (event.actionY + diffY).toInt()

                callback.onPointer(timestamp, pointer.id, x, y, pointer.type, false, targetElementPath, true)

                if (event.pointerCount == 2) {
                    lastRotationData?.let { reportRotation(timestamp, it, slPointerIds, targetElementPath, true) }
                    lastPinchData?.let { reportPinch(timestamp, it, slPointerIds, targetElementPath, true) }

                    lastRotationData = null
                    lastPinchData = null
                } else if (isPotentialTap && timestamp - potentialTapDownTimestamp <= ViewConfiguration.getTapTimeout()) {
                    potentialMultiTapPointerIds += pointer.id

                    if (isPotentialMultiTap) {
                        handler.cancelTap()
                        handler.cancelMultiTap()
                        handler.waitForMultiTap(timestamp, potentialMultiTapPointerIds.toIntArray(), targetElementPath, legacyData)
                    } else
                        handler.waitForTap(timestamp, pointer.id, targetElementPath, legacyData)

                    potentialMultiTapPreviousTapUpTimestamp = timestamp
                }

                isPotentialMultiTap = false
                isPotentialLongPress = false
                isPotentialTap = false

                handler.cancelLongPress()
            }
            MotionEvent.ACTION_CANCEL -> {
                clearPointers()
            }
        }
    }

    fun onMotionEvent(event: MotionEvent, targetElementPath: List<ElementNodeInfo>?) {
        val timestamp = System.currentTimeMillis()
        val diffX = event.rawX - event.x
        val diffY = event.rawY - event.y

        when (event.actionMasked and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_HOVER_ENTER -> {
                val pointer = updateOrCreatePointer(event, diffX, diffY)
                callback.onPointer(timestamp, pointer.id, pointer.x, pointer.y, pointer.type, true, targetElementPath, false)
            }
            MotionEvent.ACTION_HOVER_MOVE -> {
                val pointer = activePointers[event.pointerId] ?: return
                val x = (event.actionX + diffX).toInt()
                val y = (event.actionY + diffY).toInt()

                if (pointer.x != x || pointer.y != y) {
                    pointer.x = x
                    pointer.y = y

                    callback.onPointer(timestamp, pointer.id, pointer.x, pointer.y, pointer.type, true, targetElementPath, false)
                }
            }
            MotionEvent.ACTION_HOVER_EXIT -> {
                if (event.buttonState == 0) {
                    val pointer = activePointers.remove(event.pointerId) ?: return
                    val x = (event.actionX + diffX).toInt()
                    val y = (event.actionY + diffY).toInt()

                    callback.onPointer(timestamp, pointer.id, x, y, pointer.type, true, targetElementPath, true)
                }
            }
        }
    }

    private fun clearPointers() {
        val timestamp = System.currentTimeMillis()

        for ((_, pointer) in activePointers)
            callback.onPointer(timestamp, pointer.id, pointer.x, pointer.y, pointer.type, false, null, true)

        handler.cancelLongPress()

        potentialMultiTapPointerIds.clear()
        activePointers.clear()

        lastRotationData = null
        lastPinchData = null
        isPotentialMultiTap = false
        isPotentialLongPress = false
        isPotentialTap = false
    }

    private fun updateOrCreatePointer(event: MotionEvent, diffX: Float, diffY: Float): Pointer {
        val pointerId = event.pointerId
        var pointer = activePointers[pointerId]

        val x = (event.actionX + diffX).toInt()
        val y = (event.actionY + diffY).toInt()

        if (pointer == null) {
            val slPointerId = pointerIdCounter++
            val pointerType = event.getPointerType(event.actionIndex)

            pointer = Pointer(slPointerId, x, y, pointerType)
            activePointers[pointerId] = pointer
        } else {
            pointer.x = x
            pointer.y = y
        }

        return pointer
    }

    private fun MotionEvent.getPointerType(index: Int): Callback.PointerType {
        return when (getToolType(index)) {
            MotionEvent.TOOL_TYPE_FINGER -> Callback.PointerType.FINGER
            MotionEvent.TOOL_TYPE_STYLUS -> Callback.PointerType.STYLUS
            MotionEvent.TOOL_TYPE_MOUSE -> Callback.PointerType.MOUSE
            MotionEvent.TOOL_TYPE_ERASER -> Callback.PointerType.ERASER
            else -> Callback.PointerType.UNKNOWN
        }
    }

    private fun updateAndReportPointerMove(timestamp: Long, event: MotionEvent, diffX: Float, diffY: Float, targetElementPath: List<ElementNodeInfo>?) {
        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            val pointer = activePointers[pointerId] ?: continue

            val x = (event.getX(i) + diffX).toInt()
            val y = (event.getY(i) + diffY).toInt()

            if (pointer.x != x || pointer.y != y) {
                pointer.x = x
                pointer.y = y

                callback.onPointer(timestamp, pointer.id, x, y, pointer.type, false, targetElementPath, false)
            }
        }
    }

    private fun reportRotation(timestamp: Long, rotationData: RotationData, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, isLast: Boolean) {
        callback.onRotation(
            timestamp = timestamp,
            pointerIds = pointerIds,
            targetElementPath = targetElementPath,
            focusX = rotationData.focus.x.toInt(),
            focusY = rotationData.focus.y.toInt(),
            angle = rotationData.angle,
            isLast = isLast
        )
    }

    private fun reportPinch(timestamp: Long, pinchData: PinchData, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, isLast: Boolean) {
        callback.onPinch(
            timestamp = timestamp,
            pointerIds = pointerIds,
            targetElementPath = targetElementPath,
            focusX = pinchData.focus.x.toInt(),
            focusY = pinchData.focus.y.toInt(),
            distance = pinchData.distance,
            isLast = isLast
        )
    }

    private fun calcFocusPoint(event: MotionEvent, diffX: Float, diffY: Float): PointF? {
        val isTouchUp = event.isTouchUp

        if (isTouchUp && event.pointerCount == 1)
            return null

        var sumX = 0f
        var sumY = 0f
        var count = 0

        for (i in 0 until min(event.pointerCount, 2)) {
            if (isTouchUp && event.actionIndex == i)
                continue

            sumX += event.getX(i)
            sumY += event.getY(i)
            count++
        }

        val x = sumX / count + diffX
        val y = sumY / count + diffY
        return PointF(x, y)
    }

    private fun calcRotation(event: MotionEvent, lastRotation: RotationData?, diffX: Float, diffY: Float): RotationData? {
        val focusPoint = calcFocusPoint(event, diffX, diffY) ?: return null
        val isTouchUp = event.isTouchUp

        val pointers = ArrayList<RotationData.Pointer>(event.pointerCount)
        var angleDeltaSum = 0f

        for (i in 0 until event.pointerCount) {
            if (isTouchUp && event.actionIndex == i)
                continue

            val x = (focusPoint.x - event.getX(i)).toDouble()
            val y = (focusPoint.y - event.getY(i)).toDouble()

            val id = event.getPointerId(i)
            var angle = Math.toDegrees(atan2(y, x)).toFloat()

            if (y - focusPoint.y < 0f)
                angle += 180f

            var lastAngle = lastRotation?.pointers?.find { it.id == id }?.angle ?: angle

            if (abs(lastAngle - angle) > 270f)
                lastAngle = 360f - lastAngle

            pointers += RotationData.Pointer(id, angle)
            angleDeltaSum += angle - lastAngle
        }

        val angleDelta = angleDeltaSum / pointers.size
        val angle = (lastRotation?.angle ?: 0f) + angleDelta
        return RotationData(angle, focusPoint, pointers)
    }

    private fun calcPinch(event: MotionEvent, lastPinch: PinchData?, diffX: Float, diffY: Float): PinchData? {
        val focusPoint = calcFocusPoint(event, diffX, diffY) ?: return null
        val isTouchUp = event.isTouchUp

        val pointers = ArrayList<PinchData.Pointer>(event.pointerCount)
        var distanceDeltaSum = 0f

        for (i in 0 until event.pointerCount) {
            if (isTouchUp && event.actionIndex == i)
                continue

            val x = focusPoint.x - event.getX(i)
            val y = focusPoint.y - event.getY(i)

            val id = event.getPointerId(i)
            val distance = sqrt(x * x + y * y)
            val lastDistance = lastPinch?.pointers?.find { it.id == id }?.distance ?: distance

            pointers += PinchData.Pointer(id, distance)
            distanceDeltaSum += distance - lastDistance
        }

        val distanceDelta = distanceDeltaSum / pointers.size
        val distance = ((lastPinch?.distance ?: 0) + distanceDelta).toInt()
        return PinchData(distance, focusPoint, pointers)
    }

    private fun isDistanceValidForTap(event: MotionEvent, diffX: Float, diffY: Float): Boolean {
        val deltaX = event.x - potentialTapDownX + diffX
        val deltaY = event.y - potentialTapDownY + diffY
        val distance = deltaX * deltaX + deltaY * deltaY
        return distance <= config.scaledTouchSlop * config.scaledTouchSlop
    }

    @SuppressLint("HandlerLeak")
    private inner class LongTapHandler : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                TAP_MESSAGE -> {
                    val data = msg.obj as TapData
                    callback.onTap(data.timestamp, data.slPointerId, data.targetElementPath, data.legacyData)
                }
                MULTI_TAP_MESSAGE -> {
                    val data = msg.obj as MultiTapData

                    if (data.slPointerIds.size == 2)
                        callback.onDoubleTap(data.timestamp, data.slPointerIds, data.targetElementPath)
                    else
                        callback.onRageTap(data.timestamp, data.slPointerIds, data.targetElementPath, data.legacyData)
                }
                LONG_PRESS_MESSAGE -> {
                    val data = msg.obj as LongPressData
                    val timestamp = System.currentTimeMillis()
                    val pointer = activePointers[data.pointerId] ?: return

                    callback.onLongPress(timestamp, pointer.id, data.targetElementPath)
                }
            }
        }

        fun waitForTap(timestamp: Long, slPointerId: Int, targetElementPath: List<ElementNodeInfo>?, legacyData: LegacyData) {
            val data = TapData(timestamp, slPointerId, targetElementPath, legacyData)
            val message = Message.obtain(this, TAP_MESSAGE, data)
            val delay = (ViewConfiguration.getDoubleTapTimeout() + ViewConfiguration.getTapTimeout()).toLong()
            sendMessageDelayed(message, delay)
        }

        fun waitForMultiTap(timestamp: Long, slPointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, legacyData: LegacyData) {
            val data = MultiTapData(timestamp, slPointerIds, targetElementPath, legacyData)
            val message = Message.obtain(this, MULTI_TAP_MESSAGE, data)
            val delay = (ViewConfiguration.getDoubleTapTimeout() + ViewConfiguration.getTapTimeout()).toLong()
            sendMessageDelayed(message, delay)
        }

        fun waitForLongPress(pointerId: Int, targetElementPath: List<ElementNodeInfo>?) {
            val data = LongPressData(pointerId, targetElementPath)
            val message = Message.obtain(this, LONG_PRESS_MESSAGE, data)
            val delay = ViewConfiguration.getLongPressTimeout().toLong()
            sendMessageDelayed(message, delay)
        }

        fun cancelTap() {
            removeMessages(TAP_MESSAGE)
        }

        fun cancelMultiTap() {
            removeMessages(MULTI_TAP_MESSAGE)
        }

        fun cancelLongPress() {
            removeMessages(LONG_PRESS_MESSAGE)
        }
    }

    private class Pointer(val id: Int, var x: Int, var y: Int, val type: Callback.PointerType)

    private class TapData(val timestamp: Long, val slPointerId: Int, val targetElementPath: List<ElementNodeInfo>?, val legacyData: LegacyData)

    private class MultiTapData(val timestamp: Long, val slPointerIds: IntArray, val targetElementPath: List<ElementNodeInfo>?, val legacyData: LegacyData)

    private class LongPressData(val pointerId: Int, val targetElementPath: List<ElementNodeInfo>?)

    private class RotationData(val angle: Float, val focus: PointF, val pointers: List<Pointer>) {
        class Pointer(val id: Int, val angle: Float)
    }

    private class PinchData(val distance: Int, val focus: PointF, val pointers: List<Pointer>) {
        class Pointer(val id: Int, val distance: Float)
    }

    interface Callback {

        enum class SwipeDirection { LEFT, RIGHT, UP, DOWN }

        enum class PointerType { FINGER, MOUSE, STYLUS, ERASER, UNKNOWN }

        fun onPointer(timestamp: Long, pointerId: Int, x: Int, y: Int, type: PointerType, isHovering: Boolean, targetElementPath: List<ElementNodeInfo>?, isLast: Boolean)

        fun onTap(timestamp: Long, pointerId: Int, targetElementPath: List<ElementNodeInfo>?, legacyData: LegacyData)

        fun onDoubleTap(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?)

        fun onLongPress(timestamp: Long, pointerId: Int, targetElementPath: List<ElementNodeInfo>?)

        fun onRageTap(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, legacyData: LegacyData)

        fun onRotation(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, focusX: Int, focusY: Int, angle: Float, isLast: Boolean)

        fun onSwipe(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, direction: SwipeDirection)

        fun onPinch(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, focusX: Int, focusY: Int, distance: Int, isLast: Boolean)
    }

    private companion object {
        const val TAP_MESSAGE = 0
        const val MULTI_TAP_MESSAGE = 1
        const val LONG_PRESS_MESSAGE = 2

        var pointerIdCounter = 0
    }
}
