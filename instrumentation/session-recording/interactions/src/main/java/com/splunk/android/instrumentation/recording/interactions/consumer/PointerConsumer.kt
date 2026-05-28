package com.splunk.android.instrumentation.recording.interactions.consumer

import android.content.Context
import android.view.MotionEvent
import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.interactions.EventConsumer
import com.splunk.android.instrumentation.recording.interactions.OnInteractionListener
import com.splunk.android.instrumentation.recording.interactions.extension.composeTargetElementHolder
import com.splunk.android.instrumentation.recording.interactions.extension.createElementNodeInfoPath
import com.splunk.android.instrumentation.recording.interactions.extension.resolve
import com.splunk.android.instrumentation.recording.interactions.extension.toDirection
import com.splunk.android.instrumentation.recording.interactions.extension.toIntArray
import com.splunk.android.instrumentation.recording.interactions.extension.toPointerType
import com.splunk.android.instrumentation.recording.interactions.gesture.GestureDetector
import com.splunk.android.instrumentation.recording.interactions.model.ElementNode
import com.splunk.android.instrumentation.recording.interactions.model.ElementNodeInfo
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.interactions.model.LegacyData
import com.splunk.android.instrumentation.recording.interactions.util.InteractionIdProvider
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

internal class PointerConsumer(listener: OnInteractionListener) : EventConsumer(listener) {

    private val onGestureListener = TheOnGestureListener()

    private var gestureDetector: GestureDetector? = null
    private var wireframeFrame: Wireframe.Frame? = null

    override fun onWireframeUpdated(frame: Wireframe.Frame) {
        wireframeFrame = frame
    }

    override fun onTouchEvent(rootView: View, targetView: View?, event: MotionEvent) {
        val targetElementPath = if (targetView != null) {
            val path = targetView.createElementNodeInfoPath()

            if (targetView.javaClass == ANDROID_COMPOSE_VIEW_CLASS) {
                val holder = targetView.composeTargetElementHolder
                val elementHash = holder?.elementHash

                if (elementHash != null)
                    path += ElementNodeInfo(elementHash.toString(), holder.positionInList, null)
            }

            path
        } else
            null

        val legacyData = LegacyData.create(rootView, targetView)
        val context = rootView.context

        obtainGestureDetector(context).onTouchEvent(event, targetElementPath, legacyData)
    }

    override fun onMotionEvent(rootView: View, targetView: View?, event: MotionEvent) {
        val targetElementId = targetView?.createElementNodeInfoPath()
        val context = rootView.context

        obtainGestureDetector(context).onMotionEvent(event, targetElementId)
    }

    private fun obtainGestureDetector(context: Context): GestureDetector {
        if (gestureDetector == null)
            gestureDetector = GestureDetector(context, onGestureListener)

        return gestureDetector!!
    }

    private inner class TheOnGestureListener : GestureDetector.Callback {

        override fun onPointer(timestamp: Long, pointerId: Int, x: Int, y: Int, type: GestureDetector.Callback.PointerType, isHovering: Boolean, targetElementPath: List<ElementNodeInfo>?, isLast: Boolean) {
            listener.onInteraction(Interaction.Touch.Pointer(InteractionIdProvider.next(), timestamp, pointerId, x, y, type.toPointerType(), isHovering, resolveElementPath(targetElementPath, wireframeFrame), isLast))
        }

        override fun onTap(timestamp: Long, pointerId: Int, targetElementPath: List<ElementNodeInfo>?, legacyData: LegacyData) {
            listener.onInteraction(Interaction.Touch.Gesture.Tap(InteractionIdProvider.next(), timestamp, pointerId.toIntArray(), resolveElementPath(targetElementPath, wireframeFrame)), legacyData)
        }

        override fun onDoubleTap(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?) {
            listener.onInteraction(Interaction.Touch.Gesture.DoubleTap(InteractionIdProvider.next(), timestamp, pointerIds, resolveElementPath(targetElementPath, wireframeFrame)))
        }

        override fun onLongPress(timestamp: Long, pointerId: Int, targetElementPath: List<ElementNodeInfo>?) {
            listener.onInteraction(Interaction.Touch.Gesture.LongPress(InteractionIdProvider.next(), timestamp, pointerId.toIntArray(), resolveElementPath(targetElementPath, wireframeFrame)))
        }

        override fun onRotation(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, focusX: Int, focusY: Int, angle: Float, isLast: Boolean) {
            listener.onInteraction(Interaction.Touch.Gesture.Rotation(InteractionIdProvider.next(), timestamp, pointerIds, resolveElementPath(targetElementPath, wireframeFrame), focusX, focusY, angle, isLast))
        }

        override fun onSwipe(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, direction: GestureDetector.Callback.SwipeDirection) {
            listener.onInteraction(Interaction.Touch.Gesture.Swipe(InteractionIdProvider.next(), timestamp, pointerIds, resolveElementPath(targetElementPath, wireframeFrame), direction.toDirection()))
        }

        override fun onPinch(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, focusX: Int, focusY: Int, distance: Int, isLast: Boolean) {
            listener.onInteraction(Interaction.Touch.Gesture.Pinch(InteractionIdProvider.next(), timestamp, pointerIds, resolveElementPath(targetElementPath, wireframeFrame), focusX, focusY, distance, isLast))
        }

        override fun onRageTap(timestamp: Long, pointerIds: IntArray, targetElementPath: List<ElementNodeInfo>?, legacyData: LegacyData) {
            listener.onInteraction(Interaction.Touch.Gesture.RageTap(InteractionIdProvider.next(), timestamp, pointerIds, resolveElementPath(targetElementPath, wireframeFrame)), legacyData)
        }

        private fun resolveElementPath(targetElementPath: List<ElementNodeInfo>?, wireframeFrame: Wireframe.Frame?): List<ElementNode>? {
            return targetElementPath?.resolve(wireframeFrame ?: return null)
        }
    }

    private companion object {
        val ANDROID_COMPOSE_VIEW_CLASS = "androidx.compose.ui.platform.AndroidComposeView".toClass()
    }
}
