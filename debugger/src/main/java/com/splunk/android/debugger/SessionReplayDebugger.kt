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

package com.splunk.android.debugger

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import android.widget.Toast
import com.splunk.rum.common.utils.adapters.ActivityLifecycleCallbacksAdapter
import com.splunk.rum.common.utils.extensions.contentView
import com.splunk.rum.common.utils.extensions.doOnLayout
import com.splunk.rum.common.utils.window.WindowCallbackManager
import com.splunk.android.debugger.model.Location
import com.splunk.android.debugger.model.Shortcut
import com.splunk.android.debugger.screen.SettingsActivity
import com.splunk.android.debugger.screen.screenshot.ScreenshotActivity
import com.splunk.android.debugger.screen.wireframe.WireframeActivity
import com.splunk.android.debugger.util.Preferences
import com.splunk.android.debugger.window.DebugWindow
import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.instrumentation.recording.core.api.SessionReplay
import com.splunk.android.instrumentation.recording.interactions.Interactions
import com.splunk.android.instrumentation.recording.interactions.OnInteractionListener
import com.splunk.android.instrumentation.recording.interactions.extension.sampled
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.interactions.model.LegacyData
import com.splunk.android.instrumentation.recording.screenshot.ScreenshotConstructor
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot
import com.splunk.android.instrumentation.recording.screenshot.stats.ScreenshotStats
import com.splunk.android.instrumentation.recording.wireframe.WireframeExtractor
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats

internal object SessionReplayDebugger {

    private val window = DebugWindow()

    private lateinit var preferences: Preferences

    private var application: Application? = null
    private var isCloseUntilNextStartEnabled = false
    private var activitiesCount = 0

    fun attach(application: Application) {
        this.application = application

        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        preferences = Preferences.getInstance(application)

        window.isPreviewRecordingEnabled = preferences.isPreviewRecordingEnabled
        window.isInteractionPreviewEnabled = preferences.isInteractionPreviewEnabled
        window.isScreenshotPreviewEnabled = preferences.isScreenshotPreviewEnabled
        window.screenshotPreviewLocation = preferences.screenshotPreviewLocation
        window.screenshotPreviewScale = preferences.screenshotPreviewScale
        window.isScreenshotViewBoundsEnabled = preferences.isScreenshotViewBoundsEnabled
        window.isScreenshotInteractionsEnabled = preferences.isScreenshotInteractionsEnabled
        window.isScreenshotStatsEnabled = preferences.isScreenshotStatsEnabled
        window.screenshotStatsScale = preferences.screenshotStatsScale
        window.screenshotStatsLocation = preferences.screenshotStatsLocation
        window.isWireframePreviewSkeletonsEnabled = preferences.isWireframePreviewSkeletonsEnabled
        window.isWireframePreviewViewBoundsEnabled = preferences.isWireframePreviewViewBoundsEnabled
        window.wireframePreviewScale = preferences.wireframePreviewScale
        window.wireframePreviewLocation = preferences.wireframePreviewLocation
        window.isWireframeStatsEnabled = preferences.isWireframeStatsEnabled
        window.wireframeStatsScale = preferences.wireframeStatsScale
        window.wireframeStatsLocation = preferences.wireframeStatsLocation
        window.isWireframeViewBoundsOverlayEnabled = preferences.isWireframeViewBoundsOverlayEnabled
        window.isWireframeSkeletonsOverlayEnabled = preferences.isWireframeSkeletonsOverlayEnabled
        window.wireframeSkeletonsOverlayAlpha = preferences.wireframeSkeletonsOverlayAlpha
        window.listener = popupListener

        ScreenshotConstructor.isDebugModeEnabled = preferences.isScreenshotDebugModeEnabled
        WireframeExtractor.isCanvasCallsLoggingEnabled = preferences.isWireframeCanvasCallsLoggingEnabled
        WireframeExtractor.isUnknownViewsSimulationEnabled = preferences.isWireframeUnknownViewsSimulationEnabled

        Interactions.attach(application)
        Interactions.listeners += object : OnInteractionListener {
            override fun onInteraction(interaction: Interaction, legacyData: LegacyData?) {
                window.updateInteractions(Interactions.interactionsHolder.getInteractions())
            }
        }

        FrameCapturer.attach(application)
        FrameCapturer.listeners += frameCapturerListener

        if (FrameCapturer.frameHolder.wireframeFramesCountLimit == 0)
            FrameCapturer.frameHolder.wireframeFramesCountLimit = 1

        if (FrameCapturer.frameHolder.screenshotsCountLimit == 0)
            FrameCapturer.frameHolder.screenshotsCountLimit = 1

        if (preferences.isScreenshotHistoryOverrideEnabled)
            FrameCapturer.frameHolder.screenshotsCountLimit = preferences.screenshotHistorySize

        if (preferences.isWireframeHistoryOverrideEnabled)
            FrameCapturer.frameHolder.wireframeFramesCountLimit = preferences.wireframeHistorySize

        WindowCallbackManager.filters += object : WindowCallbackManager.ViewFilter {
            override fun isRejected(rootView: View): Boolean {
                return window.rootView === rootView
            }
        }
    }

    private val frameCapturerListener = object : FrameCapturer.Listener {
        override fun onNewWireframe(frame: Wireframe.Frame, stats: WireframeStats) {
            window.updateWireframe(frame, stats)
        }

        override fun onNewScreenshot(screenshot: Screenshot, stats: ScreenshotStats) {
            window.updateScreenshot(screenshot, stats)
        }
    }

    private val popupListener = object : DebugWindow.Listener {

        override fun onWheelVisibilityChanged(context: Context, isVisible: Boolean) {
            window.isRecording = SessionReplay.instance.state.status.isRecording
        }

        override fun toggleRecording(context: Context) {
            if (SessionReplay.instance.state.status.isRecording) {
                window.isRecording = false
                SessionReplay.instance.stop()
            } else {
                window.isRecording = true
                SessionReplay.instance.start()
            }
        }

        override fun showWireframeDetail(context: Context) {
            val wireframeFrames = FrameCapturer.frameHolder.getWireframeFrames()
            val unknownViewClasses = WireframeExtractor.unknownViewClasses
            val interactions = Interactions.interactionsHolder.getInteractions().sampled()

            if (wireframeFrames.isNotEmpty())
                WireframeActivity.start(context, wireframeFrames, unknownViewClasses, interactions)
        }

        override fun showScreenshotDetail(context: Context) {
            val screenshots = FrameCapturer.frameHolder.getScreenshots()
            val wireframeFrames = FrameCapturer.frameHolder.getWireframeFrames()
            val interactions = Interactions.interactionsHolder.getInteractions().sampled()

            if (screenshots.isNotEmpty())
                ScreenshotActivity.start(context, screenshots, wireframeFrames, interactions)
        }

        override fun showLog(context: Context) {
            Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
        }

        override fun openSettings(context: Context) {
            SettingsActivity.start(
                context = context,
                isPreviewRecordingEnabled = preferences.isPreviewRecordingEnabled,
                mode = preferences.mode,
                maxFrameRate = preferences.maxFrameRate,
                isInteractionPreviewEnabled = preferences.isInteractionPreviewEnabled,
                isScreenshotDebugModeEnabled = preferences.isScreenshotDebugModeEnabled,
                isScreenshotHistoryOverrideEnabled = preferences.isScreenshotHistoryOverrideEnabled,
                screenshotHistorySize = preferences.screenshotHistorySize,
                isScreenshotPreviewEnabled = preferences.isScreenshotPreviewEnabled,
                screenshotPreviewLocation = preferences.screenshotPreviewLocation,
                screenshotPreviewScale = preferences.screenshotPreviewScale,
                isScreenshotViewBoundsEnabled = preferences.isScreenshotViewBoundsEnabled,
                isScreenshotInteractionsEnabled = preferences.isScreenshotInteractionsEnabled,
                isScreenshotStatsEnabled = preferences.isScreenshotStatsEnabled,
                screenshotStatsLocation = preferences.screenshotStatsLocation,
                screenshotStatsScale = preferences.screenshotStatsScale,
                isWireframeHistoryOverrideEnabled = preferences.isWireframeHistoryOverrideEnabled,
                wireframeHistorySize = preferences.wireframeHistorySize,
                isWireframePreviewSkeletonsEnabled = preferences.isWireframePreviewSkeletonsEnabled,
                isWireframePreviewViewBoundsEnabled = preferences.isWireframePreviewViewBoundsEnabled,
                wireframePreviewLocation = preferences.wireframePreviewLocation,
                wireframePreviewScale = preferences.wireframePreviewScale,
                isWireframeStatsEnabled = preferences.isWireframeStatsEnabled,
                wireframeStatsLocation = preferences.wireframeStatsLocation,
                wireframeStatsScale = preferences.wireframeStatsScale,
                isWireframeViewBoundsOverlayEnabled = preferences.isWireframeViewBoundsOverlayEnabled,
                isWireframeSkeletonsOverlayEnabled = preferences.isWireframeSkeletonsOverlayEnabled,
                wireframeSkeletonsOverlayAlpha = preferences.wireframeSkeletonsOverlayAlpha,
                isWireframeLoggingCanvasCallsEnabled = preferences.isWireframeCanvasCallsLoggingEnabled,
                isWireframeSimulateUnknownViewsEnabled = preferences.isWireframeUnknownViewsSimulationEnabled
            )
        }

        override fun openSdk(context: Context) {
            // TODO SessionReplay
//            SdkActivity.start(context)
        }

        override fun executeShortcut(context: Context) {
            when (preferences.shortcut) {
                Shortcut.WIREFRAME_DETAIL -> showWireframeDetail(context)
                Shortcut.SCREENSHOT_DETAIL -> showScreenshotDetail(context)
                Shortcut.TOGGLE_RECORDING -> toggleRecording(context)
                Shortcut.SHOW_LOG -> showLog(context)
                null -> Unit
            }
        }

        override fun updateShortcut(context: Context, shortcut: Shortcut) {
            preferences.shortcut = shortcut
        }
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacksAdapter {

        override fun onActivityStarted(activity: Activity) {
            activitiesCount++
        }

        override fun onActivityResumed(activity: Activity) {
            if (isBuiltInActivity(activity)) {
                if (activity is SettingsActivity)
                    activity.listener = settingsListener

                // TODO SessionReplay
//                if (activity is SdkActivity)
//                    activity.listener = sdkListener

                return
            }

            activity.contentView?.doOnLayout {
                if (!isCloseUntilNextStartEnabled && !activity.isFinishing && !activity.window.isFloating && !activity.isDestroyed)
                    window.show(activity)
            }
        }

        override fun onActivityStopped(activity: Activity) {
            activitiesCount--

            if (isBuiltInActivity(activity)) {
                if (activity is SettingsActivity)
                    activity.listener = null

                // TODO SessionReplay
//                if (activity is SdkActivity)
//                    activity.listener = null

                return
            }

            if (activitiesCount == 0 || window.isAttachedToActivity(activity))
                window.hide()
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activitiesCount == 0)
                isCloseUntilNextStartEnabled = false
        }

        private fun isBuiltInActivity(activity: Activity): Boolean {
            return activity is WireframeActivity || activity is ScreenshotActivity || activity is SettingsActivity // // TODO SessionReplay || activity is SdkActivity
        }
    }

    // TODO SessionReplay
//    private val sdkListener = object : SdkActivity.Listener {
//
//        override fun onRecordingChanged(isRecording: Boolean) {
//            window.isRecording = isRecording
//        }
//    }

    private val settingsListener = object : SettingsActivity.Listener {
        override fun onPreviewRecordingEnabledChanged(context: Context, isEnabled: Boolean) {
            preferences.isPreviewRecordingEnabled = isEnabled
            window.isPreviewRecordingEnabled = isEnabled
        }

        override fun onMode(context: Context, mode: FrameCapturer.Mode) {
            preferences.mode = mode
            FrameCapturer.mode = mode
        }

        override fun onMaxFrameRateChanged(context: Context, value: Int) {
            preferences.maxFrameRate = value
            FrameCapturer.maxFrameRate = value
        }

        override fun onInteractionPreviewEnabledChanged(context: Context, isEnabled: Boolean) {
            preferences.isInteractionPreviewEnabled = isEnabled
            window.isInteractionPreviewEnabled = isEnabled
        }

        override fun onScreenshotHistoryOverrideEnabledChanged(context: Context, isEnabled: Boolean) {
            preferences.isScreenshotHistoryOverrideEnabled = isEnabled

            if (isEnabled)
                FrameCapturer.frameHolder.screenshotsCountLimit = preferences.screenshotHistorySize
        }

        override fun onScreenshotHistorySizeChanged(context: Context, size: Int) {
            preferences.screenshotHistorySize = size

            if (preferences.isScreenshotHistoryOverrideEnabled)
                FrameCapturer.frameHolder.screenshotsCountLimit = size
        }

        override fun onScreenshotHistoryClear(context: Context) {
            FrameCapturer.frameHolder.clearScreenshots()
        }

        override fun onScreenshotDebugModeEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isScreenshotDebugModeEnabled = isEnabled
            ScreenshotConstructor.isDebugModeEnabled = isEnabled
        }

        override fun onScreenshotPreviewEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isScreenshotPreviewEnabled = isEnabled
            window.isScreenshotPreviewEnabled = isEnabled
        }

        override fun onScreenshotViewBordersEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isScreenshotViewBoundsEnabled = isEnabled
            window.isScreenshotViewBoundsEnabled = isEnabled
        }

        override fun onScreenshotInteractionsEnabledChanged(context: Context, isEnabled: Boolean) {
            preferences.isScreenshotInteractionsEnabled = isEnabled
            window.isScreenshotInteractionsEnabled = isEnabled
        }

        override fun onScreenshotPreviewLocationChanged(context: Context, location: Location) {
            preferences.screenshotPreviewLocation = location
            window.screenshotPreviewLocation = location
        }

        override fun onScreenshotPreviewScaleChanged(context: Context, scale: Float) {
            preferences.screenshotPreviewScale = scale
            window.screenshotPreviewScale = scale
        }

        override fun onScreenshotStatsEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isScreenshotStatsEnabled = isEnabled
            window.isScreenshotStatsEnabled = isEnabled
        }

        override fun onScreenshotStatsLocationChanged(context: Context, location: Location) {
            preferences.screenshotStatsLocation = location
            window.screenshotStatsLocation = location
        }

        override fun onScreenshotStatsScaleChanged(context: Context, scale: Float) {
            preferences.screenshotStatsScale = scale
            window.screenshotStatsScale = scale
        }

        override fun onWireframeHistoryOverrideEnabledChanged(context: Context, isEnabled: Boolean) {
            preferences.isWireframeHistoryOverrideEnabled = isEnabled

            if (isEnabled)
                FrameCapturer.frameHolder.wireframeFramesCountLimit = preferences.wireframeHistorySize
        }

        override fun onWireframeHistorySizeChanged(context: Context, size: Int) {
            preferences.wireframeHistorySize = size

            if (preferences.isWireframeHistoryOverrideEnabled)
                FrameCapturer.frameHolder.wireframeFramesCountLimit = size
        }

        override fun onWireframeHistoryClear(context: Context) {
            FrameCapturer.frameHolder.clearWireframeFrames()
        }

        override fun onWireframePreviewSkeletonsEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isWireframePreviewSkeletonsEnabled = isEnabled
            window.isWireframePreviewSkeletonsEnabled = isEnabled
        }

        override fun onWireframePreviewViewBordersEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isWireframePreviewViewBoundsEnabled = isEnabled
            window.isWireframePreviewViewBoundsEnabled = isEnabled
        }

        override fun onWireframePreviewLocationChanged(context: Context, location: Location) {
            preferences.wireframePreviewLocation = location
            window.wireframePreviewLocation = location
        }

        override fun onWireframePreviewScaleChanged(context: Context, scale: Float) {
            preferences.wireframePreviewScale = scale
            window.wireframePreviewScale = scale
        }

        override fun onWireframeStatsEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isWireframeStatsEnabled = isEnabled
            window.isWireframeStatsEnabled = isEnabled
        }

        override fun onWireframeStatsLocationChanged(context: Context, location: Location) {
            preferences.wireframeStatsLocation = location
            window.wireframeStatsLocation = location
        }

        override fun onWireframeStatsScaleChanged(context: Context, scale: Float) {
            preferences.wireframeStatsScale = scale
            window.wireframeStatsScale = scale
        }

        override fun onWireframeViewBoundsOverlayEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isWireframeViewBoundsOverlayEnabled = isEnabled
            window.isWireframeViewBoundsOverlayEnabled = isEnabled
        }

        override fun onWireframeSkeletonsOverlayEnabledChanged(context: Context, isEnabled: Boolean) {
            preferences.isWireframeSkeletonsOverlayEnabled = isEnabled
            window.isWireframeSkeletonsOverlayEnabled = isEnabled
        }

        override fun onWireframeSkeletonsOverlayAlphaChanged(context: Context, alpha: Float) {
            preferences.wireframeSkeletonsOverlayAlpha = alpha
            window.wireframeSkeletonsOverlayAlpha = alpha
        }

        override fun onWireframeLoggingCanvasCallsEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isWireframeCanvasCallsLoggingEnabled = isEnabled
            WireframeExtractor.isCanvasCallsLoggingEnabled = isEnabled
        }

        override fun onWireframeSimulateUnknownViewsEnableChanged(context: Context, isEnabled: Boolean) {
            preferences.isWireframeUnknownViewsSimulationEnabled = isEnabled
            WireframeExtractor.isUnknownViewsSimulationEnabled = isEnabled
        }

        override fun onCloseUntilNextStartClicked(context: Context) {
            isCloseUntilNextStartEnabled = true
            window.hide(false)
        }
    }
}
