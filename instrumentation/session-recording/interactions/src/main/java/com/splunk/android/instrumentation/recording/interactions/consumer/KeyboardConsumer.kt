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

package com.splunk.android.instrumentation.recording.interactions.consumer

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import android.widget.FrameLayout
import com.splunk.rum.common.utils.RootViewObserver
import com.splunk.rum.common.utils.dpToPx
import com.splunk.rum.common.utils.extensions.activity
import com.splunk.rum.common.utils.extensions.doOnDraw
import com.splunk.rum.common.utils.extensions.windowManager
import com.splunk.rum.common.utils.window.WindowCallbackManager
import com.splunk.rum.common.utils.window.WindowCallbackManager.ViewFilter
import com.splunk.android.instrumentation.recording.interactions.EventConsumer
import com.splunk.android.instrumentation.recording.interactions.OnInteractionListener
import com.splunk.android.instrumentation.recording.interactions.R
import com.splunk.android.instrumentation.recording.interactions.extension.isInvisibleForInteractions
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.interactions.util.InteractionIdProvider
import com.splunk.android.instrumentation.recording.screenshot.extension.isInvisibleForScreenshot
import com.splunk.android.instrumentation.recording.wireframe.extension.isInvisibleForWireframe
import java.util.LinkedList

// FIXME Open Focus screen > Focus EditText > Focus NumberPicker - Two keyboards are displayed (keyboard size change?)
// TODO Memory leak reported on Android 13 - false positive
internal class KeyboardConsumer(listener: OnInteractionListener) : EventConsumer(listener) {

    private val locationOnScreen = IntArray(2)
    private var keyboardRect: Rect? = null

    private var layoutObserver: LayoutObserver? = null
    private val rootViews = LinkedList<View>()

    private val handler = Handler(Looper.getMainLooper())

    private val reattachRunnable = Runnable { reattach() }

    override fun onRootViewAdded(rootView: View) {
        if (isObserverView(rootView))
            return

        rootViews += rootView

        rootView.doOnDraw {
            reattach()
        }
    }

    override fun onRootViewRemoved(rootView: View) {
        if (isObserverView(rootView))
            return

        rootViews -= rootView
        reattach()
    }

    private fun reattach() {
        handler.removeCallbacks(reattachRunnable)

        if (RootViewObserver.isSafeToRemoveRootView()) {
            detachObserver()
            attachObserver()
        } else
            handler.post(reattachRunnable)
    }

    private fun attachObserver() {
        for (i in rootViews.indices.reversed()) {
            val activity = rootViews[i].activity ?: continue

            if (activity.isFinishing)
                continue

            val layoutObserver = LayoutObserver(activity)

            try {
                layoutObserver.attach()
            } catch (_: BadTokenException) { // Unable to add window -- token android.os.BinderProxy@618b12c is not valid; is your activity running?
                break
            }

            this.layoutObserver = layoutObserver
            break
        }
    }

    private fun detachObserver() {
        try {
            layoutObserver?.detach()
        } catch (_: IllegalArgumentException) {
            // An exception can be thrown and I do not why. Maybe when a View in an application is removed from background thread?
            // Reported in https://github.com/smartlook/smartlook-support/issues/734 from com.optimumbrewlab.invitationcardmaker application v65
        }

        layoutObserver = null
    }

    private fun isObserverView(view: View): Boolean {
        return view.id == R.id.sl_reference_view || view.id == R.id.sl_observer_view
    }

    private fun reportKeyboardRectChange(rect: Rect?) {
        if (rect == keyboardRect)
            return

        keyboardRect = rect

        val id = InteractionIdProvider.next()
        val timestamp = System.currentTimeMillis()
        val interaction = Interaction.Keyboard(id, timestamp, rect)

        listener.onInteraction(interaction)
    }

    private inner class LayoutObserver(
        context: Context
    ) : OnLayoutChangeListener {

        private val referenceView = FrameLayout(context)
        private val observerView = FrameLayout(context)

        init {
            referenceView.id = R.id.sl_reference_view
            referenceView.isInvisibleForWireframe = true
            referenceView.isInvisibleForScreenshot = true
            referenceView.isInvisibleForInteractions = true

            observerView.id = R.id.sl_observer_view
            observerView.isInvisibleForWireframe = true
            observerView.isInvisibleForScreenshot = true
            observerView.isInvisibleForInteractions = true

            referenceView.addOnLayoutChangeListener(this)
            observerView.addOnLayoutChangeListener(this)

            if (DEBUG) {
                referenceView.background = ColorDrawable(Color.GREEN)
                observerView.background = ColorDrawable(Color.RED)
            } else {
                referenceView.alpha = 0f
                observerView.alpha = 0f
            }
        }

        fun attach() {
            val width = if (DEBUG) dpToPx(5f) else VIEWS_WIDTH

            // The following Views must be added in exact order due to a system bug.
            addView(observerView, width, OBSERVER_FLAGS, OBSERVER_GRAVITY)
            addView(referenceView, width, REFERENCE_FLAGS, REFERENCE_GRAVITY)
        }

        fun detach() {
            val windowManager = referenceView.context.windowManager

            referenceView.removeOnLayoutChangeListener(this)
            observerView.removeOnLayoutChangeListener(this)

            if (referenceView.parent != null)
                windowManager.removeViewImmediate(referenceView)

            if (observerView.parent != null)
                windowManager.removeViewImmediate(observerView)
        }

        private fun addView(view: View, width: Int, flags: Int, gravity: Int) {
            val type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW
            val layoutParams = WindowManager.LayoutParams(width, WindowManager.LayoutParams.MATCH_PARENT, type, flags, PixelFormat.TRANSPARENT)
            layoutParams.gravity = gravity

            view.context.windowManager.addView(view, layoutParams)
        }

        override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            assessKeyboardVisibility()
        }

        private fun assessKeyboardVisibility() {
            val referenceHeight = referenceView.height
            val observerHeight = observerView.height

            if (referenceHeight == 0 || observerHeight == 0)
                return

            val rect = if (observerHeight < referenceHeight) {
                observerView.getLocationOnScreen(locationOnScreen)

                val left = 0
                val top = locationOnScreen[1] + observerView.bottom
                val right = locationOnScreen[0] + observerView.right
                val bottom = locationOnScreen[1] + referenceView.bottom

                Rect(left, top, right, bottom)
            } else
                null

            reportKeyboardRectChange(rect)
        }
    }

    private companion object {

        const val DEBUG = false

        const val REFERENCE_FLAGS = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        const val REFERENCE_GRAVITY = Gravity.LEFT or Gravity.TOP

        const val OBSERVER_FLAGS = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        const val OBSERVER_GRAVITY = Gravity.RIGHT or Gravity.TOP

        const val VIEWS_WIDTH = 1

        init {
            WindowCallbackManager.filters += object : ViewFilter {
                override fun isRejected(rootView: View): Boolean {
                    return rootView.id == R.id.sl_reference_view || rootView.id == R.id.sl_observer_view
                }
            }
        }
    }
}
