package com.splunk.android.debugger.util

import android.content.Context
import android.preference.PreferenceManager
import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.debugger.extension.getEnum
import com.splunk.android.debugger.extension.putEnum
import com.splunk.android.debugger.model.Location
import com.splunk.android.debugger.model.Shortcut

@Suppress("DEPRECATION")
internal class Preferences(context: Context) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext) // FIXME separate

    var shortcut: Shortcut?
        get() = preferences.getEnum("sld_shortcut", Shortcut.WIREFRAME_DETAIL)
        set(value) = preferences.edit().putEnum("sld_shortcut", value).apply()

    var isPreviewRecordingEnabled: Boolean
        get() = preferences.getBoolean("sld_isPreviewRecordingEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isPreviewRecordingEnabled", value).apply()

    var mode: FrameCapturer.Mode
        get() = preferences.getEnum("sld_mode", FrameCapturer.Mode.WIREFRAME_SCREENSHOT)
        set(value) = preferences.edit().putEnum("sld_mode", value).apply()

    var maxFrameRate: Int
        get() = preferences.getInt("sld_maxFrameRate", 2)
        set(value) = preferences.edit().putInt("sld_maxFrameRate", value).apply()

    var isInteractionPreviewEnabled: Boolean
        get() = preferences.getBoolean("sld_isInteractionPreviewEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isInteractionPreviewEnabled", value).apply()

    var isScreenshotDebugModeEnabled: Boolean
        get() = preferences.getBoolean("sld_isScreenshotDebugModeEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isScreenshotDebugModeEnabled", value).apply()

    var isScreenshotHistoryOverrideEnabled: Boolean
        get() = preferences.getBoolean("sld_isScreenshotHistoryOverrideEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isScreenshotHistoryOverrideEnabled", value).apply()

    var screenshotHistorySize: Int
        get() = preferences.getInt("sld_screenshotHistorySize", 1)
        set(value) = preferences.edit().putInt("sld_screenshotHistorySize", value).apply()

    var isScreenshotPreviewEnabled: Boolean
        get() = preferences.getBoolean("sld_isScreenshotPreviewEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isScreenshotPreviewEnabled", value).apply()

    var isScreenshotViewBoundsEnabled: Boolean
        get() = preferences.getBoolean("sld_isScreenshotViewBoundsEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isScreenshotViewBoundsEnabled", value).apply()

    var isScreenshotInteractionsEnabled: Boolean
        get() = preferences.getBoolean("sld_isScreenshotInteractionsEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isScreenshotInteractionsEnabled", value).apply()

    var screenshotPreviewLocation: Location
        get() = preferences.getEnum("sld_screenshotPreviewLocation", Location.TOP_LEFT)
        set(value) = preferences.edit().putEnum("sld_screenshotPreviewLocation", value).apply()

    var screenshotPreviewScale: Float
        get() = preferences.getFloat("sld_screenshotPreviewScale", 0.5f)
        set(value) = preferences.edit().putFloat("sld_screenshotPreviewScale", value).apply()

    var isScreenshotStatsEnabled: Boolean
        get() = preferences.getBoolean("sld_isScreenshotStatsEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isScreenshotStatsEnabled", value).apply()

    var screenshotStatsLocation: Location
        get() = preferences.getEnum("sld_screenshotStatsLocation", Location.TOP_RIGHT)
        set(value) = preferences.edit().putEnum("sld_screenshotStatsLocation", value).apply()

    var screenshotStatsScale: Float
        get() = preferences.getFloat("sld_screenshotStatsScale", 0.8f)
        set(value) = preferences.edit().putFloat("sld_screenshotStatsScale", value).apply()

    var isWireframeHistoryOverrideEnabled: Boolean
        get() = preferences.getBoolean("sld_isWireframeHistoryOverrideEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isWireframeHistoryOverrideEnabled", value).apply()

    var wireframeHistorySize: Int
        get() = preferences.getInt("sld_wireframeHistorySize", 40)
        set(value) = preferences.edit().putInt("sld_wireframeHistorySize", value).apply()

    var isWireframePreviewSkeletonsEnabled: Boolean
        get() = preferences.getBoolean("sld_isWireframePreviewSkeletonsEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isWireframePreviewSkeletonsEnabled", value).apply()

    var isWireframePreviewViewBoundsEnabled: Boolean
        get() = preferences.getBoolean("sld_isWireframePreviewViewBoundsEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isWireframePreviewViewBoundsEnabled", value).apply()

    var wireframePreviewLocation: Location
        get() = preferences.getEnum("sld_wireframePreviewLocation", Location.TOP_LEFT)
        set(value) = preferences.edit().putEnum("sld_wireframePreviewLocation", value).apply()

    var wireframePreviewScale: Float
        get() = preferences.getFloat("sld_wireframePreviewScale", 0.5f)
        set(value) = preferences.edit().putFloat("sld_wireframePreviewScale", value).apply()

    var isWireframeStatsEnabled: Boolean
        get() = preferences.getBoolean("sld_isWireframeStatsEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isWireframeStatsEnabled", value).apply()

    var wireframeStatsLocation: Location
        get() = preferences.getEnum("sld_wireframeStatsLocation", Location.TOP_RIGHT)
        set(value) = preferences.edit().putEnum("sld_wireframeStatsLocation", value).apply()

    var wireframeStatsScale: Float
        get() = preferences.getFloat("sld_wireframeStatsScale", 0.8f)
        set(value) = preferences.edit().putFloat("sld_wireframeStatsScale", value).apply()

    var isWireframeViewBoundsOverlayEnabled: Boolean
        get() = preferences.getBoolean("sld_isWireframeViewBoundsOverlayEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isWireframeViewBoundsOverlayEnabled", value).apply()

    var isWireframeSkeletonsOverlayEnabled: Boolean
        get() = preferences.getBoolean("sld_isWireframeSkeletonsOverlayEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isWireframeSkeletonsOverlayEnabled", value).apply()

    var wireframeSkeletonsOverlayAlpha: Float
        get() = preferences.getFloat("sld_wireframeSkeletonsOverlayAlpha", 0.5f)
        set(value) = preferences.edit().putFloat("sld_wireframeSkeletonsOverlayAlpha", value).apply()

    var isWireframeCanvasCallsLoggingEnabled: Boolean
        get() = preferences.getBoolean("sld_isWireframeCanvasCallsLoggingEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isWireframeCanvasCallsLoggingEnabled", value).apply()

    var isWireframeUnknownViewsSimulationEnabled: Boolean
        get() = preferences.getBoolean("sld_isWireframeUnknownViewsSimulationEnabled", false)
        set(value) = preferences.edit().putBoolean("sld_isWireframeUnknownViewsSimulationEnabled", value).apply()

    companion object {

        private var instance: Preferences? = null

        fun getInstance(context: Context): Preferences {
            if (instance == null)
                instance = Preferences(context)

            return instance!!
        }
    }
}
