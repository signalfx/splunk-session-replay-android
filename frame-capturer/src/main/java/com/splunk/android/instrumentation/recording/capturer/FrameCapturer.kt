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

package com.splunk.android.instrumentation.recording.capturer

import android.app.Activity
import android.app.Application
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import com.splunk.android.bridge.BridgeManager
import com.splunk.android.instrumentation.recording.capturer.utils.FrameRateManager
import com.splunk.android.instrumentation.recording.screenshot.ScreenshotConstructor
import com.splunk.android.instrumentation.recording.screenshot.extension.createEmpty
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot
import com.splunk.android.instrumentation.recording.screenshot.stats.ScreenshotStats
import com.splunk.android.instrumentation.recording.wireframe.WireframeConstructor
import com.splunk.android.instrumentation.recording.wireframe.extension.createEmpty
import com.splunk.android.instrumentation.recording.wireframe.extension.isDrawDeterministic
import com.splunk.android.instrumentation.recording.wireframe.extension.rect
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats
import com.splunk.rum.common.utils.Lock
import com.splunk.rum.common.utils.adapters.ActivityLifecycleCallbacksAdapter
import java.lang.ref.WeakReference

/* FIXME
 *  - Popup exit animation is missing
 *  - Drawable animations is not a draw trigger
 *  - App > Compose Surface > Open debugger > Close debugger - stuck
 *  - Close frame is one frame behind
 *  - Old Wireframe when tap on "View ordering"
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object FrameCapturer {

    enum class Mode {
        NONE, WIREFRAME, WIREFRAME_SCREENSHOT
    }

    private val listener = ConstructorListener()
    private val wireframeConstructor = WireframeConstructor(listener)
    private val screenshotConstructor = ScreenshotConstructor(listener)
    private val frameRateManager = FrameRateManager()

    private var application: Application? = null
    private var currentActivity = WeakReference<Activity>(null)

    private var isModeSet = false

    private var isFirstFrameDropped = false
    private var isFrameSkipped = false

    val frameHolder = FrameHolder()

    val listeners: MutableCollection<Listener> = HashSet()

    var screenMasksProvider: ScreenMasksProvider? = null

    var maxFrameRate: Int
        get() = frameRateManager.maxFrameRate
        set(value) = run { frameRateManager.maxFrameRate = value }

    @set:MainThread
    var mode = Mode.NONE
        set(value) {
            if (value == field && isModeSet)
                return

            isModeSet = true
            field = value

            val rect = frameHolder.getLastWireframeFrame()?.rect ?: getDisplayRect()
            val time = System.currentTimeMillis()

            if (value == Mode.NONE) {
                frameRateManager.isInstantReportEnabled = false

                wireframeConstructor.clear()
                screenshotConstructor.clear()

                val wireframe = Wireframe.Frame.createEmpty(rect, time)
                val wireframeStats = WireframeStats.createEmpty()

                val screenshot = Screenshot.createEmpty(rect, time)
                val screenshotStats = ScreenshotStats.createEmpty()

                frameHolder.storeWireframeFrame(wireframe)
                frameHolder.storeScreenshot(screenshot)

                for (listener in listeners) {
                    listener.onNewWireframe(wireframe, wireframeStats)
                    listener.onNewScreenshot(screenshot, screenshotStats)
                }
            } else {
                if (value == Mode.WIREFRAME) {
                    screenshotConstructor.clear()

                    val screenshot = Screenshot.createEmpty(rect, time)
                    val screenshotStats = ScreenshotStats.createEmpty()

                    frameHolder.storeScreenshot(screenshot)

                    listeners.forEach { it.onNewScreenshot(screenshot, screenshotStats) }
                }

                frameRateManager.requestNewFrame(null)
            }
        }

    fun attach(application: Application) {
        if (this.application != null)
            return

        this.application = application
        application.registerActivityLifecycleCallbacks(activityLifecycleCallback)

        frameRateManager.listener = frameRateManagerListener
        frameRateManager.attach(application)
    }

    fun requestNewFrame(view: View? = null) {
        frameRateManager.requestNewFrame(view)
    }

    private fun getDisplayRect(): Rect {
        val displayMetrics = Resources.getSystem().displayMetrics
        return Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    private val frameRateManagerListener = object : FrameRateManager.Listener {

        override fun isRecordingAllowed(): Boolean {
            return BridgeManager.isRecordingAllowed
        }

        override fun onNewFrame() {
            if (mode != Mode.NONE) {
                isFrameSkipped = !isFirstFrameDropped
                isFirstFrameDropped = true

                if (isFrameSkipped)
                    return

                val context = currentActivity.get() ?: requireNotNull(application)
                wireframeConstructor.openNewFrame(context)

                if (mode == Mode.WIREFRAME_SCREENSHOT)
                    screenshotConstructor.openNewFrame()
            }
        }

        override fun onViewChanged(view: View): Boolean {
            if (isFrameSkipped)
                return false

            if (mode != Mode.NONE) {
                val window = wireframeConstructor.updateView(view)

                if (mode == Mode.WIREFRAME_SCREENSHOT)
                    screenshotConstructor.updateView(view, window)

                return window?.isDrawDeterministic ?: true
            }

            return true
        }

        override fun onViewRemoved(view: View) {
            if (isFrameSkipped)
                return

            if (mode != Mode.NONE) {
                wireframeConstructor.removeView(view)

                if (mode == Mode.WIREFRAME_SCREENSHOT)
                    screenshotConstructor.removeView(view)
            }
        }

        override fun onCloseFrame() {
            if (isFrameSkipped) {
                isFrameSkipped = false
                return
            }

            if (mode != Mode.NONE)
                wireframeConstructor.closeFrame()
        }
    }

    private val activityLifecycleCallback = object : ActivityLifecycleCallbacksAdapter {
        override fun onActivityResumed(activity: Activity) {
            currentActivity = WeakReference(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            if (currentActivity.get() === activity)
                currentActivity.clear()
        }
    }

    private class ConstructorListener : ScreenshotConstructor.Listener, WireframeConstructor.Listener {

        private val lock = Lock()

        private var preFrame: Wireframe.Frame? = null
        private var midFrame: Wireframe.Frame? = null
        private var postFrame: Wireframe.Frame? = null

        override fun onNewFrame(frame: Wireframe.Frame, stats: WireframeStats, isChanged: Boolean) {
            if (frame.scenes.first().rect.isEmpty) { // There is no any visible content (debug tool screens)
                frameRateManager.isInstantReportEnabled = false
                preFrame = null
                midFrame = null
                lock.unlock()
                return
            }

            if (midFrame == null || postFrame != null)
                frameHolder.storeWireframeFrame(frame, preFrame != null && postFrame == null)

            if (isChanged)
                listeners.forEach { it.onNewWireframe(frame, stats) }

            if (mode == Mode.WIREFRAME)
                return

            when {
                preFrame == null -> {
                    frameRateManager.isInstantReportEnabled = true
                    preFrame = frame
                }
                midFrame == null -> {
                    screenshotConstructor.screenMasks = screenMasksProvider?.onScreenMasksRequested()
                    midFrame = frame
                    lock.lock()

                    val isProcessed = screenshotConstructor.closeFrame(frame)
                    frameRateManager.isInstantReportEnabled = isProcessed

                    if (!isProcessed) {
                        preFrame = null
                        midFrame = null
                    }
                }
                postFrame == null -> {
                    frameRateManager.isInstantReportEnabled = false
                    postFrame = frame
                    lock.unlock()
                }
            }
        }

        override fun onNewScreenshot(screenshot: Screenshot?, stats: ScreenshotStats, isChanged: Boolean) {
            frameRateManager.isInstantReportEnabled = false
            preFrame = null
            midFrame = null
            postFrame = null
            lock.unlock()

            if (isChanged && screenshot != null) {
                frameHolder.storeScreenshot(screenshot)

                for (listener in listeners)
                    listener.onNewScreenshot(screenshot, stats)
            }
        }

        override fun onRequestPreFrame(): Wireframe.Frame? {
            return preFrame
        }

        override fun onRequestMidFrame(): Wireframe.Frame? {
            return midFrame
        }

        override fun onRequestPostFrame(): Wireframe.Frame? {
            if (postFrame == null)
                lock.waitToUnlock()

            return postFrame
        }
    }

    interface Listener {
        fun onNewWireframe(frame: Wireframe.Frame, stats: WireframeStats) {}
        fun onNewScreenshot(screenshot: Screenshot, stats: ScreenshotStats) {}
    }
}
