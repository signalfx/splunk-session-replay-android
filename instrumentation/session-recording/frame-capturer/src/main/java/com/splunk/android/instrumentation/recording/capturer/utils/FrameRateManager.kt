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

package com.splunk.android.instrumentation.recording.capturer.utils

import android.app.Activity
import android.app.Application
import android.os.Looper
import android.os.SystemClock
import android.view.Choreographer
import android.view.View
import android.view.ViewTreeObserver
import com.splunk.android.common.utils.AppStateObserver
import com.splunk.android.common.utils.RootViewObserver
import com.splunk.android.common.utils.adapters.ActivityLifecycleCallbacksAdapter
import com.splunk.android.common.utils.extensions.rootView
import com.splunk.android.instrumentation.recording.capturer.R

internal class FrameRateManager {

    private val choreographer = Choreographer.getInstance()
    private val appStateObserver = AppStateObserver()
    private val pendingChangedViews = HashSet<View>()
    private val pausedViews = HashSet<View>()

    private var isFragmentTransactionRunning = false
    private var isViewTransitionRunning = false
    private var lastCaptureTime = 0L
    private var isDrawFrame = false

    private val frameInterval: Int
        get() = 1000 / maxFrameRate

    var maxFrameRate = 2

    @Volatile
    var isInstantReportEnabled = false

    var listener: Listener? = null

    fun attach(application: Application) {
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        appStateObserver.listener = appStateObserverListener
        appStateObserver.attach(application)

        RootViewObserver.listeners += rootViewObserverListener
        RootViewObserver.attach(application)
    }

    fun requestNewFrame(view: View?) {
        lastCaptureTime = 0L

        if (view != null)
            pendingChangedViews += view.rootView
        else
            for (view in RootViewObserver.views)
                pendingChangedViews += view
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacksAdapter {

        override fun onActivityResumed(activity: Activity) {
            val view = activity.rootView ?: return
            pausedViews -= view
        }

        override fun onActivityPaused(activity: Activity) {
            val view = activity.rootView ?: return
            pendingChangedViews -= view
            pausedViews += view
        }

        override fun onActivityStopped(activity: Activity) {
            val view = activity.rootView ?: return
            pausedViews -= view
        }
    }

    private val appStateObserverListener = object : AppStateObserver.Listener {
        override fun onAppBackgrounded() {
            choreographer.removeFrameCallback(frameCallback)
        }

        override fun onAppForegrounded() {
            choreographer.postFrameCallback(frameCallback)
            isDrawFrame = false
        }

        override fun onFragmentTransactionStarted() {
            isFragmentTransactionRunning = true
        }

        override fun onFragmentTransactionEnded() {
            isFragmentTransactionRunning = false
        }

        override fun onViewTransitionStarted() {
            isViewTransitionRunning = true
        }

        override fun onViewTransitionEnded() {
            isViewTransitionRunning = false
        }
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)

            if (listener?.isRecordingAllowed() == false || isFragmentTransactionRunning || isViewTransitionRunning)
                return

            if (isDrawFrame) {
                listener?.onCloseFrame()
                isDrawFrame = false
            } else if (isInstantReportEnabled || pendingChangedViews.isNotEmpty() && SystemClock.elapsedRealtime() - lastCaptureTime >= frameInterval) {
                lastCaptureTime = SystemClock.elapsedRealtime()
                listener?.onNewFrame()

                val iterator = pendingChangedViews.iterator()

                while (iterator.hasNext()) {
                    val view = iterator.next()

                    if (listener?.onViewChanged(view) == true)
                        iterator.remove()
                }

                listener?.onCloseFrame()
            }
        }
    }

    private val rootViewObserverListener = object : RootViewObserver.Listener {
        override fun onAdded(view: View) {
            val listener = OnPreDrawListener(view)
            view.onPreDrawListenerReference = listener
            view.viewTreeObserver.addOnPreDrawListener(listener)
        }

        override fun onRemoved(view: View) {
            val listener = view.onPreDrawListenerReference ?: return
            view.viewTreeObserver.removeOnPreDrawListener(listener)

            pendingChangedViews.remove(view)

            if (!isDrawFrame) {
                this@FrameRateManager.listener?.onNewFrame()
                isDrawFrame = true
            }

            this@FrameRateManager.listener?.onViewRemoved(view)
        }

        private var View.onPreDrawListenerReference: OnPreDrawListener?
            get() = getTag(R.id.sl_tag_pre_draw_listener) as? OnPreDrawListener
            set(value) = setTag(R.id.sl_tag_pre_draw_listener, value)
    }

    private inner class OnPreDrawListener(private val view: View) : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            if (Looper.myLooper() != Looper.getMainLooper()) // View.draw(Canvas) can be called from background Thread.
                return true

            if (listener?.isRecordingAllowed() != false && !isFragmentTransactionRunning && !isViewTransitionRunning && view !in pausedViews)
                if (isDrawFrame || SystemClock.elapsedRealtime() - lastCaptureTime >= frameInterval) {
                    lastCaptureTime = SystemClock.elapsedRealtime()

                    if (!isDrawFrame) {
                        listener?.onNewFrame()
                        isDrawFrame = true
                    }

                    if (listener?.onViewChanged(view) != true)
                        pendingChangedViews += view
                } else
                    pendingChangedViews += view

            return true
        }
    }

    interface Listener {

        fun isRecordingAllowed(): Boolean

        fun onNewFrame()

        /**
         * @return whether the [view] has deterministic draw.
         */
        fun onViewChanged(view: View): Boolean

        fun onViewRemoved(view: View)

        fun onCloseFrame()
    }
}
