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

package com.splunk.android.instrumentation.recording.interactions

import android.app.Activity
import android.app.Application
import android.os.Build
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.Window
import com.splunk.android.common.utils.adapters.ActivityLifecycleCallbacksAdapter
import com.splunk.android.common.utils.extensions.forEachFast
import com.splunk.android.common.utils.window.WindowCallbackManager
import com.splunk.android.common.utils.window.WindowCallbackWrapper
import com.splunk.android.instrumentation.recording.interactions.consumer.ButtonConsumer
import com.splunk.android.instrumentation.recording.interactions.consumer.FocusConsumer
import com.splunk.android.instrumentation.recording.interactions.consumer.KeyboardConsumer
import com.splunk.android.instrumentation.recording.interactions.consumer.OrientationConsumer
import com.splunk.android.instrumentation.recording.interactions.consumer.PointerConsumer
import com.splunk.android.instrumentation.recording.interactions.extension.findTouchTarget
import com.splunk.android.instrumentation.recording.interactions.extension.isInvisibleForInteractions
import com.splunk.android.instrumentation.recording.interactions.extension.isTouchUp
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.interactions.model.LegacyData
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import kotlin.reflect.KClass

object Interactions {

    private val interactionListener = TheOnInteractionListener()
    private val consumers: List<EventConsumer>

    private var isAttached = false

    val interactionsHolder = InteractionsHolder()

    val listeners: MutableCollection<OnInteractionListener> = ArrayList()

    var allowedInteractions: Set<KClass<out Interaction>> = setOf(
        Interaction.Touch.Pointer::class,
        Interaction.Touch.Gesture.RageTap::class,
        Interaction.Touch.Gesture.Tap::class,
        Interaction.Focus::class,
        Interaction.Touch.Gesture.DoubleTap::class,
        Interaction.Touch.Gesture.LongPress::class,
        Interaction.Touch.Gesture.Swipe::class,
        Interaction.Touch.Gesture.Pinch::class,
        Interaction.Touch.Gesture.Rotation::class,
        Interaction.PhoneButton::class,
        Interaction.Keyboard::class,
        // Interaction.Orientation::class
    )

    init {
        val consumers = ArrayList<EventConsumer>()

        consumers += PointerConsumer(interactionListener)
        consumers += ButtonConsumer(interactionListener)
        consumers += KeyboardConsumer(interactionListener)
        consumers += FocusConsumer(interactionListener)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            consumers += OrientationConsumer(interactionListener)

        this.consumers = consumers
    }

    fun attach(application: Application) {
        if (isAttached)
            return

        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        WindowCallbackManager.callbacks += windowCallbackManagerCallback
        WindowCallbackManager.attach(application)

        isAttached = true
    }

    fun updateWireframe(frame: Wireframe.Frame) {
        consumers.forEachFast { it.onWireframeUpdated(frame) }
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacksAdapter {
        override fun onActivityStarted(activity: Activity) {
            consumers.forEachFast { it.onActivityStarted(activity) }
        }

        override fun onActivityResumed(activity: Activity) {
            consumers.forEachFast { it.onActivityResumed(activity) }
        }

        override fun onActivityPaused(activity: Activity) {
            consumers.forEachFast { it.onActivityPaused(activity) }
        }

        override fun onActivityStopped(activity: Activity) {
            consumers.forEachFast { it.onActivityStopped(activity) }
        }
    }

    private val windowCallbackManagerCallback = object : WindowCallbackManager.Callback {

        override fun onRootViewAdded(rootView: View) {
            if (!rootView.isInvisibleForInteractions)
                consumers.forEachFast { it.onRootViewAdded(rootView) }
        }

        override fun onRootViewRemoved(rootView: View) {
            if (!rootView.isInvisibleForInteractions)
                consumers.forEachFast { it.onRootViewRemoved(rootView) }
        }

        override fun wrapWindowCallback(rootView: View, callback: Window.Callback?): Window.Callback {
            return InteractionsCallback(rootView, callback)
        }
    }

    private class TheOnInteractionListener : OnInteractionListener {
        override fun onInteraction(interaction: Interaction, legacyData: LegacyData?) {
            if (interaction::class !in allowedInteractions)
                return

            interactionsHolder.storeInteraction(interaction, legacyData)
            listeners.forEachFast { it.onInteraction(interaction, legacyData) }
        }
    }

    private class InteractionsCallback(private val rootView: View, callback: Window.Callback?) : WindowCallbackWrapper(callback) {

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (!rootView.isInvisibleForInteractions)
                consumers.forEachFast { it.onKeyEvent(rootView, event) }

            return super.dispatchKeyEvent(event)
        }

        override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
            if (!rootView.isInvisibleForInteractions)
                consumers.forEachFast { it.onMotionEvent(rootView, null, event) }

            return super.dispatchGenericMotionEvent(event)
        }

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            if (rootView.isInvisibleForInteractions)
                return super.dispatchTouchEvent(event)

            val isConsumed: Boolean
            var targetView: View?

            if (event.isTouchUp) {
                targetView = rootView.findTouchTarget()
                isConsumed = super.dispatchTouchEvent(event)

                if (!isConsumed)
                    targetView = null
            } else {
                isConsumed = super.dispatchTouchEvent(event)
                targetView = if (isConsumed) rootView.findTouchTarget() else null
            }

            consumers.forEachFast { it.onTouchEvent(rootView, targetView, event) }
            return isConsumed
        }
    }
}
