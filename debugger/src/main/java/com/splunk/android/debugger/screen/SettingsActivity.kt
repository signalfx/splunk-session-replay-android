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

package com.splunk.android.debugger.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.rum.common.utils.extensions.contentView
import com.splunk.android.debugger.R
import com.splunk.android.debugger.databinding.SldActivitySettingsBinding
import com.splunk.android.debugger.model.Location
import com.splunk.android.debugger.view.LocationView
import com.splunk.android.instrumentation.recording.interactions.extension.isInvisibleForInteractions
import com.splunk.android.instrumentation.recording.screenshot.extension.isInvisibleForScreenshot
import com.splunk.android.instrumentation.recording.wireframe.extension.isInvisibleForWireframe

// FIXME Tap near to out of window does not close it (inset)
// FIXME Android 16 compatibility
internal class SettingsActivity : Activity() {

    private lateinit var viewBinding: SldActivitySettingsBinding

    var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        viewBinding = SldActivitySettingsBinding.inflate(layoutInflater, contentView, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            viewBinding.root.foreground = ResourcesCompat.getDrawable(resources, R.drawable.sld_ic_gradient_white, theme)

        setupSelection()
        setupListeners()

        isInvisibleForWireframe = true
        isInvisibleForScreenshot = true
        isInvisibleForInteractions = true
    }

    private fun setupSelection() {
        viewBinding.recordPreview.isChecked = intent.getBooleanExtra(EXTRA_RECORD_PREVIEW, false)

        val mode = intent.getSerializableExtra(EXTRA_MODE) as FrameCapturer.Mode
        viewBinding.mode.check(getViewForMode(mode).id)

        val maxFramerate = intent.getIntExtra(EXTRA_MAX_FRAME_RATE, 2)
        viewBinding.maxFramerate.progress = maxFramerate - 1
        viewBinding.maxFramerateLabel.text = getString(R.string.sld_settings_max_framerate_label_format, maxFramerate)

        val isInteractionPreviewEnabled = intent.getBooleanExtra(EXTRA_INTERACTION_PREVIEW_ENABLED, false)
        viewBinding.interactionPreview.isChecked = isInteractionPreviewEnabled

        val isScreenshotDebugModeEnabled = intent.getBooleanExtra(EXTRA_SCREENSHOT_DEBUG_MODE_ENABLED, false)
        viewBinding.screenshotDebugMode.isChecked = isScreenshotDebugModeEnabled

        val isScreenshotHistoryOverrideEnabled = intent.getBooleanExtra(EXTRA_SCREENSHOT_HISTORY_OVERRIDE_ENABLED, false)
        viewBinding.screenshotHistoryOverrideEnabled.isChecked = isScreenshotHistoryOverrideEnabled

        val screenshotHistorySize = intent.getIntExtra(EXTRA_SCREENSHOT_HISTORY_SIZE, 40)
        viewBinding.screenshotHistorySize.progress = screenshotHistorySize - 1

        viewBinding.screenshotHistorySizeLabel.text = if (screenshotHistorySize == Int.MAX_VALUE) getString(R.string.sld_settings_screenshot_history_size_label_unlimited) else getString(R.string.sld_settings_screenshot_history_size_label_format, screenshotHistorySize)

        val isScreenshotPreviewImageEnabled = intent.getBooleanExtra(EXTRA_SCREENSHOT_PREVIEW_ENABLED, false)
        viewBinding.screenshotPreviewImage.isChecked = isScreenshotPreviewImageEnabled

        val isScreenshotPreviewViewBoundsEnabled = intent.getBooleanExtra(EXTRA_SCREENSHOT_VIEW_BOUNDS_ENABLED, false)
        viewBinding.screenshotPreviewViewBounds.isChecked = isScreenshotPreviewViewBoundsEnabled

        val isScreenshotPreviewVInteractionsEnabled = intent.getBooleanExtra(EXTRA_SCREENSHOT_INTERACTIONS_ENABLED, false)
        viewBinding.screenshotPreviewInteractions.isChecked = isScreenshotPreviewVInteractionsEnabled

        val screenshotPreviewScale = intent.getFloatExtra(EXTRA_SCREENSHOT_PREVIEW_SCALE, 1f)
        val screenshotPreviewProgress = (screenshotPreviewScale - PREVIEW_SCALE_MIN) / (1f - PREVIEW_SCALE_MIN)
        viewBinding.screenshotPreviewScale.progress = (screenshotPreviewProgress * viewBinding.screenshotPreviewScale.max).toInt()

        val screenshotPreviewLocation = intent.getSerializableExtra(EXTRA_SCREENSHOT_PREVIEW_LOCATION) as Location
        viewBinding.screenshotPreviewLocation.location = screenshotPreviewLocation

        val isScreenshotRenderStatsEnabled = intent.getBooleanExtra(EXTRA_SCREENSHOT_STATS_ENABLED, false)
        viewBinding.screenshotStats.isChecked = isScreenshotRenderStatsEnabled

        val screenshotStatsScale = intent.getFloatExtra(EXTRA_SCREENSHOT_STATS_SCALE, 1f)
        val screenshotStatsProgress = (screenshotStatsScale - STATS_SCALE_MIN) / (1f - STATS_SCALE_MIN)
        viewBinding.screenshotStatsScale.progress = (screenshotStatsProgress * viewBinding.screenshotStatsScale.max).toInt()

        val screenshotRenderStatsLocation = intent.getSerializableExtra(EXTRA_SCREENSHOT_STATS_LOCATION) as Location
        viewBinding.screenshotStatsLocation.location = screenshotRenderStatsLocation

        val isWireframeHistoryOverrideEnabled = intent.getBooleanExtra(EXTRA_WIREFRAME_HISTORY_OVERRIDE_ENABLED, false)
        viewBinding.wireframeHistoryOverrideEnabled.isChecked = isWireframeHistoryOverrideEnabled

        val wireframeHistorySize = intent.getIntExtra(EXTRA_WIREFRAME_HISTORY_SIZE, 40)
        viewBinding.wireframeHistorySize.progress = wireframeHistorySize - 1
        viewBinding.wireframeHistorySizeLabel.text = if (wireframeHistorySize == Int.MAX_VALUE) getString(R.string.sld_settings_wireframe_history_size_label_unlimited) else getString(R.string.sld_settings_wireframe_history_size_label_format, wireframeHistorySize)

        val isWireframePreviewSkeletonsEnabled = intent.getBooleanExtra(EXTRA_WIREFRAME_PREVIEW_SKELETONS, false)
        viewBinding.wireframePreviewSkeletons.isChecked = isWireframePreviewSkeletonsEnabled

        val isWireframePreviewViewBoundsEnabled = intent.getBooleanExtra(EXTRA_WIREFRAME_PREVIEW_VIEW_BOUNDS, false)
        viewBinding.wireframePreviewViewBounds.isChecked = isWireframePreviewViewBoundsEnabled

        val wireframePreviewScale = intent.getFloatExtra(EXTRA_WIREFRAME_PREVIEW_SCALE, 1f)
        val wireframePreviewProgress = (wireframePreviewScale - PREVIEW_SCALE_MIN) / (1f - PREVIEW_SCALE_MIN)
        viewBinding.wireframePreviewScale.progress = (wireframePreviewProgress * viewBinding.wireframePreviewScale.max).toInt()

        val wireframePreviewLocation = intent.getSerializableExtra(EXTRA_WIREFRAME_PREVIEW_LOCATION) as Location
        viewBinding.wireframePreviewLocation.location = wireframePreviewLocation

        val isWireframeStatsEnabled = intent.getBooleanExtra(EXTRA_WIREFRAME_STATS_ENABLED, false)
        viewBinding.wireframeStats.isChecked = isWireframeStatsEnabled

        val wireframeStatsScale = intent.getFloatExtra(EXTRA_WIREFRAME_STATS_SCALE, 1f)
        val wireframeStatsProgress = (wireframeStatsScale - STATS_SCALE_MIN) / (1f - STATS_SCALE_MIN)
        viewBinding.wireframeStatsScale.progress = (wireframeStatsProgress * viewBinding.wireframeStatsScale.max).toInt()

        val wireframeStatsLocation = intent.getSerializableExtra(EXTRA_WIREFRAME_STATS_LOCATION) as Location
        viewBinding.wireframeStatsLocation.location = wireframeStatsLocation

        val isWireframeViewBoundsOverlayEnabled = intent.getBooleanExtra(EXTRA_WIREFRAME_VIEW_BOUNDS_OVERLAY_ENABLED, false)
        viewBinding.wireframeViewBoundsOverlay.isChecked = isWireframeViewBoundsOverlayEnabled

        val isWireframeSkeletonsOverlayEnabled = intent.getBooleanExtra(EXTRA_WIREFRAME_SKELETONS_OVERLAY_ENABLED, false)
        viewBinding.wireframeSkeletonsOverlay.isChecked = isWireframeSkeletonsOverlayEnabled

        val wireframeSkeletonsOverlayAlpha = intent.getFloatExtra(EXTRA_WIREFRAME_SKELETONS_OVERLAY_ALPHA, 0.5f)
        viewBinding.wireframeSkeletonsOverlayAlpha.progress = (wireframeSkeletonsOverlayAlpha * viewBinding.wireframeSkeletonsOverlayAlpha.max).toInt()

        val isWireframeLoggingCanvasCallsEnabled = intent.getBooleanExtra(EXTRA_WIREFRAME_LOGGING_CANVAS_CALLS_ENABLED, false)
        viewBinding.wireframeLogCanvasCalls.isChecked = isWireframeLoggingCanvasCallsEnabled

        val isWireframeSimulateUnknownViewsEnabled = intent.getBooleanExtra(EXTRA_WIREFRAME_SIMULATE_UNKNOWN_VIEWS_ENABLED, false)
        viewBinding.wireframeSimulateUnknownViews.isChecked = isWireframeSimulateUnknownViewsEnabled

        updateVisibilities()
    }

    private fun setupListeners() {
        viewBinding.exit.setOnClickListener(onClickListener)

        viewBinding.recordPreview.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.mode.setOnCheckedChangeListener(radioGroupOnCheckedChangedListener)

        viewBinding.interactionPreview.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.screenshotDebugMode.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.screenshotHistoryOverrideEnabled.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.screenshotPreviewImage.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.screenshotPreviewViewBounds.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.screenshotPreviewInteractions.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.screenshotStats.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.wireframeHistoryOverrideEnabled.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.wireframePreviewSkeletons.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.wireframePreviewViewBounds.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.wireframeStats.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.wireframeViewBoundsOverlay.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.wireframeSkeletonsOverlay.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.wireframeLogCanvasCalls.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)
        viewBinding.wireframeSimulateUnknownViews.setOnCheckedChangeListener(compoundButtonOnCheckedChangedListener)

        viewBinding.screenshotPreviewLocation.listener = locationViewListener
        viewBinding.screenshotStatsLocation.listener = locationViewListener
        viewBinding.wireframePreviewLocation.listener = locationViewListener
        viewBinding.wireframeStatsLocation.listener = locationViewListener

        viewBinding.maxFramerate.setOnSeekBarChangeListener(onSeekBarChangeListener)
        viewBinding.screenshotHistorySize.setOnSeekBarChangeListener(onSeekBarChangeListener)
        viewBinding.screenshotPreviewScale.setOnSeekBarChangeListener(onSeekBarChangeListener)
        viewBinding.screenshotStatsScale.setOnSeekBarChangeListener(onSeekBarChangeListener)
        viewBinding.wireframeHistorySize.setOnSeekBarChangeListener(onSeekBarChangeListener)
        viewBinding.wireframePreviewScale.setOnSeekBarChangeListener(onSeekBarChangeListener)
        viewBinding.wireframeStatsScale.setOnSeekBarChangeListener(onSeekBarChangeListener)
        viewBinding.wireframeSkeletonsOverlayAlpha.setOnSeekBarChangeListener(onSeekBarChangeListener)

        viewBinding.screenshotHistoryClear.setOnClickListener(onClickListener)
        viewBinding.wireframeHistoryClear.setOnClickListener(onClickListener)
        viewBinding.closeUntilStart.setOnClickListener(onClickListener)
    }

    private fun updateVisibilities() {
        val screenshotHistoryVisibility = if (viewBinding.screenshotHistoryOverrideEnabled.isChecked) View.VISIBLE else View.GONE
        viewBinding.screenshotHistorySizeContainer.visibility = screenshotHistoryVisibility
        viewBinding.screenshotHistorySize.visibility = screenshotHistoryVisibility

        val screenshotPreviewVisibility = if (viewBinding.screenshotPreviewImage.isChecked) View.VISIBLE else View.GONE
        viewBinding.screenshotPreviewViewBounds.visibility = screenshotPreviewVisibility
        viewBinding.screenshotPreviewInteractions.visibility = screenshotPreviewVisibility
        viewBinding.screenshotPreviewLocationContainer.visibility = screenshotPreviewVisibility
        viewBinding.screenshotPreviewScaleContainer.visibility = screenshotPreviewVisibility

        val screenshotStatsVisibility = if (viewBinding.screenshotStats.isChecked) View.VISIBLE else View.GONE
        viewBinding.screenshotStatsLocationContainer.visibility = screenshotStatsVisibility
        viewBinding.screenshotStatsScaleContainer.visibility = screenshotStatsVisibility

        val wireframeHistoryVisibility = if (viewBinding.wireframeHistoryOverrideEnabled.isChecked) View.VISIBLE else View.GONE
        viewBinding.wireframeHistorySizeContainer.visibility = wireframeHistoryVisibility
        viewBinding.wireframeHistorySize.visibility = wireframeHistoryVisibility

        val wireframePreviewVisibility = if (viewBinding.wireframePreviewSkeletons.isChecked || viewBinding.wireframePreviewViewBounds.isChecked) View.VISIBLE else View.GONE
        viewBinding.wireframePreviewLocationContainer.visibility = wireframePreviewVisibility
        viewBinding.wireframePreviewScaleContainer.visibility = wireframePreviewVisibility

        val wireframeStatsVisibility = if (viewBinding.wireframeStats.isChecked) View.VISIBLE else View.GONE
        viewBinding.wireframeStatsLocationContainer.visibility = wireframeStatsVisibility
        viewBinding.wireframeStatsScaleContainer.visibility = wireframeStatsVisibility

        val skeletonsOverlayVisibility = if (viewBinding.wireframeSkeletonsOverlay.isChecked) View.VISIBLE else View.GONE
        viewBinding.wireframeSkeletonsOverlayAlphaContainer.visibility = skeletonsOverlayVisibility
    }

    private fun closeUntilNextStart(context: Context) {
        listener?.onCloseUntilNextStartClicked(context)
        onBackPressed()
    }

    private fun getViewForMode(mode: FrameCapturer.Mode): RadioButton {
        return when (mode) {
            FrameCapturer.Mode.NONE ->
                viewBinding.modeNone
            FrameCapturer.Mode.WIREFRAME ->
                viewBinding.modeWireframe
            FrameCapturer.Mode.WIREFRAME_SCREENSHOT ->
                viewBinding.modeWireframeScreenshot
        }
    }

    private val radioGroupOnCheckedChangedListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        val mode = when (checkedId) {
            viewBinding.modeNone.id ->
                FrameCapturer.Mode.NONE
            viewBinding.modeWireframe.id ->
                FrameCapturer.Mode.WIREFRAME
            viewBinding.modeWireframeScreenshot.id ->
                FrameCapturer.Mode.WIREFRAME_SCREENSHOT
            else ->
                throw IllegalArgumentException()
        }

        listener?.onMode(group.context, mode)
    }

    private val onClickListener = View.OnClickListener {
        when (it.id) {
            viewBinding.exit.id ->
                onBackPressed()
            viewBinding.screenshotHistoryClear.id ->
                listener?.onScreenshotHistoryClear(it.context)
            viewBinding.wireframeHistoryClear.id ->
                listener?.onWireframeHistoryClear(it.context)
            viewBinding.closeUntilStart.id ->
                closeUntilNextStart(it.context)
        }
    }

    private val compoundButtonOnCheckedChangedListener = CompoundButton.OnCheckedChangeListener { view, isChecked ->
        when (view.id) {
            viewBinding.recordPreview.id -> {
                listener?.onPreviewRecordingEnabledChanged(view.context, isChecked)
            }
            viewBinding.interactionPreview.id -> {
                listener?.onInteractionPreviewEnabledChanged(view.context, isChecked)
            }
            viewBinding.screenshotDebugMode.id -> {
                listener?.onScreenshotDebugModeEnableChanged(view.context, isChecked)
            }
            viewBinding.screenshotHistoryOverrideEnabled.id -> {
                listener?.onScreenshotHistoryOverrideEnabledChanged(view.context, isChecked)
                updateVisibilities()
            }
            viewBinding.screenshotPreviewImage.id -> {
                listener?.onScreenshotPreviewEnableChanged(view.context, isChecked)
                updateVisibilities()
            }
            viewBinding.screenshotPreviewViewBounds.id -> {
                listener?.onScreenshotViewBordersEnableChanged(view.context, isChecked)
            }
            viewBinding.screenshotPreviewInteractions.id -> {
                listener?.onScreenshotInteractionsEnabledChanged(view.context, isChecked)
            }
            viewBinding.screenshotStats.id -> {
                listener?.onScreenshotStatsEnableChanged(view.context, isChecked)
                updateVisibilities()
            }
            viewBinding.wireframeHistoryOverrideEnabled.id -> {
                listener?.onWireframeHistoryOverrideEnabledChanged(view.context, isChecked)
                updateVisibilities()
            }
            viewBinding.wireframePreviewSkeletons.id -> {
                listener?.onWireframePreviewSkeletonsEnableChanged(view.context, isChecked)
                updateVisibilities()
            }
            viewBinding.wireframePreviewViewBounds.id -> {
                listener?.onWireframePreviewViewBordersEnableChanged(view.context, isChecked)
                updateVisibilities()
            }
            viewBinding.wireframeStats.id -> {
                listener?.onWireframeStatsEnableChanged(view.context, isChecked)
                updateVisibilities()
            }
            viewBinding.wireframeLogCanvasCalls.id -> {
                listener?.onWireframeLoggingCanvasCallsEnableChanged(view.context, isChecked)
            }
            viewBinding.wireframeViewBoundsOverlay.id -> {
                listener?.onWireframeViewBoundsOverlayEnableChanged(view.context, isChecked)
            }
            viewBinding.wireframeSkeletonsOverlay.id -> {
                listener?.onWireframeSkeletonsOverlayEnabledChanged(view.context, isChecked)
                updateVisibilities()
            }
            viewBinding.wireframeSimulateUnknownViews.id -> {
                listener?.onWireframeSimulateUnknownViewsEnableChanged(view.context, isChecked)
            }
        }
    }

    private val locationViewListener = object : LocationView.Listener {
        override fun onLocationChanged(view: LocationView, location: Location?) {
            when (view.id) {
                viewBinding.screenshotPreviewLocation.id ->
                    listener?.onScreenshotPreviewLocationChanged(view.context, location ?: Location.TOP_LEFT)
                viewBinding.screenshotStatsLocation.id ->
                    listener?.onScreenshotStatsLocationChanged(view.context, location ?: Location.TOP_LEFT)
                viewBinding.wireframePreviewLocation.id ->
                    listener?.onWireframePreviewLocationChanged(view.context, location ?: Location.TOP_LEFT)
                viewBinding.wireframeStatsLocation.id ->
                    listener?.onWireframeStatsLocationChanged(view.context, location ?: Location.TOP_LEFT)
            }
        }
    }

    private val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(view: SeekBar, progress: Int, fromUser: Boolean) {
            if (!fromUser)
                return

            val fraction = progress.toFloat() / view.max

            when (view.id) {
                viewBinding.maxFramerate.id -> {
                    val maxFramerate = progress + 1
                    viewBinding.maxFramerateLabel.text = getString(R.string.sld_settings_max_framerate_label_format, maxFramerate)
                    listener?.onMaxFrameRateChanged(view.context, maxFramerate)
                }
                viewBinding.screenshotHistorySize.id -> {
                    val text: String
                    val size: Int

                    if (progress == view.max) {
                        size = Int.MAX_VALUE
                        text = getString(R.string.sld_settings_screenshot_history_size_label_unlimited)
                    } else {
                        size = progress + 1
                        text = getString(R.string.sld_settings_screenshot_history_size_label_format, size)
                    }

                    viewBinding.screenshotHistorySizeLabel.text = text
                    listener?.onScreenshotHistorySizeChanged(view.context, size)
                }
                viewBinding.screenshotPreviewScale.id -> {
                    val scale = PREVIEW_SCALE_MIN + fraction * (1f - PREVIEW_SCALE_MIN)
                    listener?.onScreenshotPreviewScaleChanged(view.context, scale)
                }
                viewBinding.screenshotStatsScale.id -> {
                    val scale = STATS_SCALE_MIN + fraction * (1f - STATS_SCALE_MIN)
                    listener?.onScreenshotStatsScaleChanged(view.context, scale)
                }
                viewBinding.wireframeHistorySize.id -> {
                    val text: String
                    val size: Int

                    if (progress == view.max) {
                        size = Int.MAX_VALUE
                        text = getString(R.string.sld_settings_wireframe_history_size_label_unlimited)
                    } else {
                        size = progress + 1
                        text = getString(R.string.sld_settings_wireframe_history_size_label_format, size)
                    }

                    viewBinding.wireframeHistorySizeLabel.text = text
                    listener?.onWireframeHistorySizeChanged(view.context, size)
                }
                viewBinding.wireframePreviewScale.id -> {
                    val scale = PREVIEW_SCALE_MIN + fraction * (1f - PREVIEW_SCALE_MIN)
                    listener?.onWireframePreviewScaleChanged(view.context, scale)
                }
                viewBinding.wireframeStatsScale.id -> {
                    val scale = STATS_SCALE_MIN + fraction * (1f - STATS_SCALE_MIN)
                    listener?.onWireframeStatsScaleChanged(view.context, scale)
                }
                viewBinding.wireframeSkeletonsOverlayAlpha.id -> {
                    val alpha = view.progress / view.max.toFloat()
                    listener?.onWireframeSkeletonsOverlayAlphaChanged(view.context, alpha)
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {}
        override fun onStopTrackingTouch(seekBar: SeekBar) {}
    }

    interface Listener {
        fun onPreviewRecordingEnabledChanged(context: Context, isEnabled: Boolean)
        fun onMode(context: Context, mode: FrameCapturer.Mode)
        fun onMaxFrameRateChanged(context: Context, value: Int)
        fun onInteractionPreviewEnabledChanged(context: Context, isEnabled: Boolean)
        fun onScreenshotHistoryOverrideEnabledChanged(context: Context, isEnabled: Boolean)
        fun onScreenshotHistorySizeChanged(context: Context, size: Int)
        fun onScreenshotHistoryClear(context: Context)
        fun onScreenshotDebugModeEnableChanged(context: Context, isEnabled: Boolean)
        fun onScreenshotPreviewEnableChanged(context: Context, isEnabled: Boolean)
        fun onScreenshotViewBordersEnableChanged(context: Context, isEnabled: Boolean)
        fun onScreenshotInteractionsEnabledChanged(context: Context, isEnabled: Boolean)
        fun onScreenshotPreviewLocationChanged(context: Context, location: Location)
        fun onScreenshotPreviewScaleChanged(context: Context, scale: Float)
        fun onScreenshotStatsEnableChanged(context: Context, isEnabled: Boolean)
        fun onScreenshotStatsLocationChanged(context: Context, location: Location)
        fun onScreenshotStatsScaleChanged(context: Context, scale: Float)
        fun onWireframeHistoryOverrideEnabledChanged(context: Context, isEnabled: Boolean)
        fun onWireframeHistorySizeChanged(context: Context, size: Int)
        fun onWireframeHistoryClear(context: Context)
        fun onWireframePreviewSkeletonsEnableChanged(context: Context, isEnabled: Boolean)
        fun onWireframePreviewViewBordersEnableChanged(context: Context, isEnabled: Boolean)
        fun onWireframePreviewLocationChanged(context: Context, location: Location)
        fun onWireframePreviewScaleChanged(context: Context, scale: Float)
        fun onWireframeStatsEnableChanged(context: Context, isEnabled: Boolean)
        fun onWireframeStatsLocationChanged(context: Context, location: Location)
        fun onWireframeStatsScaleChanged(context: Context, scale: Float)
        fun onWireframeViewBoundsOverlayEnableChanged(context: Context, isEnabled: Boolean)
        fun onWireframeSkeletonsOverlayEnabledChanged(context: Context, isEnabled: Boolean)
        fun onWireframeSkeletonsOverlayAlphaChanged(context: Context, alpha: Float)
        fun onWireframeLoggingCanvasCallsEnableChanged(context: Context, isEnabled: Boolean)
        fun onWireframeSimulateUnknownViewsEnableChanged(context: Context, isEnabled: Boolean)
        fun onCloseUntilNextStartClicked(context: Context)
    }

    companion object {

        private const val EXTRA_RECORD_PREVIEW = "EXTRA_RECORD_PREVIEW"
        private const val EXTRA_MODE = "EXTRA_MODE"
        private const val EXTRA_MAX_FRAME_RATE = "EXTRA_MAX_FRAME_RATE"
        private const val EXTRA_INTERACTION_PREVIEW_ENABLED = "EXTRA_INTERACTION_PREVIEW_ENABLED"
        private const val EXTRA_SCREENSHOT_DEBUG_MODE_ENABLED = "EXTRA_SCREENSHOT_DEBUG_MODE_ENABLED"
        private const val EXTRA_SCREENSHOT_HISTORY_OVERRIDE_ENABLED = "EXTRA_SCREENSHOT_HISTORY_OVERRIDE_ENABLED"
        private const val EXTRA_SCREENSHOT_HISTORY_SIZE = "EXTRA_SCREENSHOT_HISTORY_SIZE"
        private const val EXTRA_SCREENSHOT_PREVIEW_ENABLED = "EXTRA_SCREENSHOT_PREVIEW_ENABLED"
        private const val EXTRA_SCREENSHOT_PREVIEW_LOCATION = "EXTRA_SCREENSHOT_PREVIEW_LOCATION"
        private const val EXTRA_SCREENSHOT_PREVIEW_SCALE = "EXTRA_SCREENSHOT_PREVIEW_SCALE"
        private const val EXTRA_SCREENSHOT_VIEW_BOUNDS_ENABLED = "EXTRA_SCREENSHOT_VIEW_BOUNDS_ENABLED"
        private const val EXTRA_SCREENSHOT_INTERACTIONS_ENABLED = "EXTRA_SCREENSHOT_INTERACTIONS_ENABLED"
        private const val EXTRA_SCREENSHOT_STATS_ENABLED = "EXTRA_SCREENSHOT_STATS_ENABLED"
        private const val EXTRA_SCREENSHOT_STATS_LOCATION = "EXTRA_SCREENSHOT_STATS_LOCATION"
        private const val EXTRA_SCREENSHOT_STATS_SCALE = "EXTRA_SCREENSHOT_STATS_SCALE"
        private const val EXTRA_WIREFRAME_HISTORY_OVERRIDE_ENABLED = "EXTRA_WIREFRAME_HISTORY_OVERRIDE_ENABLED"
        private const val EXTRA_WIREFRAME_HISTORY_SIZE = "EXTRA_WIREFRAME_HISTORY_SIZE"
        private const val EXTRA_WIREFRAME_PREVIEW_LOCATION = "EXTRA_WIREFRAME_PREVIEW_LOCATION"
        private const val EXTRA_WIREFRAME_PREVIEW_SCALE = "EXTRA_WIREFRAME_PREVIEW_SCALE"
        private const val EXTRA_WIREFRAME_PREVIEW_SKELETONS = "EXTRA_WIREFRAME_PREVIEW_SKELETONS"
        private const val EXTRA_WIREFRAME_PREVIEW_VIEW_BOUNDS = "EXTRA_WIREFRAME_PREVIEW_VIEW_BOUNDS"
        private const val EXTRA_WIREFRAME_STATS_ENABLED = "EXTRA_WIREFRAME_STATS_ENABLED"
        private const val EXTRA_WIREFRAME_STATS_LOCATION = "EXTRA_WIREFRAME_STATS_LOCATION"
        private const val EXTRA_WIREFRAME_STATS_SCALE = "EXTRA_WIREFRAME_STATS_SCALE"
        private const val EXTRA_WIREFRAME_VIEW_BOUNDS_OVERLAY_ENABLED = "EXTRA_WIREFRAME_VIEW_BOUNDS_OVERLAY_ENABLED"
        private const val EXTRA_WIREFRAME_SKELETONS_OVERLAY_ENABLED = "EXTRA_WIREFRAME_SKELETONS_OVERLAY_ENABLED"
        private const val EXTRA_WIREFRAME_SKELETONS_OVERLAY_ALPHA = "EXTRA_WIREFRAME_SKELETONS_OVERLAY_ALPHA"
        private const val EXTRA_WIREFRAME_LOGGING_CANVAS_CALLS_ENABLED = "EXTRA_WIREFRAME_LOGGING_CANVAS_CALLS_ENABLED"
        private const val EXTRA_WIREFRAME_SIMULATE_UNKNOWN_VIEWS_ENABLED = "EXTRA_WIREFRAME_SIMULATE_UNKNOWN_VIEWS_ENABLED"

        private const val PREVIEW_SCALE_MIN = 0.3f
        private const val STATS_SCALE_MIN = 0.5f

        fun start(
            context: Context,
            isPreviewRecordingEnabled: Boolean,
            mode: FrameCapturer.Mode,
            maxFrameRate: Int,
            isInteractionPreviewEnabled: Boolean,
            isScreenshotDebugModeEnabled: Boolean,
            isScreenshotHistoryOverrideEnabled: Boolean,
            screenshotHistorySize: Int,
            isScreenshotPreviewEnabled: Boolean,
            screenshotPreviewLocation: Location,
            screenshotPreviewScale: Float,
            isScreenshotViewBoundsEnabled: Boolean,
            isScreenshotInteractionsEnabled: Boolean,
            isScreenshotStatsEnabled: Boolean,
            screenshotStatsLocation: Location,
            screenshotStatsScale: Float,
            isWireframeHistoryOverrideEnabled: Boolean,
            wireframeHistorySize: Int,
            isWireframePreviewSkeletonsEnabled: Boolean,
            isWireframePreviewViewBoundsEnabled: Boolean,
            wireframePreviewLocation: Location,
            wireframePreviewScale: Float,
            isWireframeStatsEnabled: Boolean,
            wireframeStatsLocation: Location,
            wireframeStatsScale: Float,
            isWireframeViewBoundsOverlayEnabled: Boolean,
            isWireframeSkeletonsOverlayEnabled: Boolean,
            wireframeSkeletonsOverlayAlpha: Float,
            isWireframeLoggingCanvasCallsEnabled: Boolean,
            isWireframeSimulateUnknownViewsEnabled: Boolean
        ) {
            val intent = Intent(context, SettingsActivity::class.java)
                .putExtra(EXTRA_RECORD_PREVIEW, isPreviewRecordingEnabled)
                .putExtra(EXTRA_MODE, mode)
                .putExtra(EXTRA_MAX_FRAME_RATE, maxFrameRate)
                .putExtra(EXTRA_INTERACTION_PREVIEW_ENABLED, isInteractionPreviewEnabled)
                .putExtra(EXTRA_SCREENSHOT_DEBUG_MODE_ENABLED, isScreenshotDebugModeEnabled)
                .putExtra(EXTRA_SCREENSHOT_HISTORY_OVERRIDE_ENABLED, isScreenshotHistoryOverrideEnabled)
                .putExtra(EXTRA_SCREENSHOT_HISTORY_SIZE, screenshotHistorySize)
                .putExtra(EXTRA_SCREENSHOT_PREVIEW_ENABLED, isScreenshotPreviewEnabled)
                .putExtra(EXTRA_SCREENSHOT_VIEW_BOUNDS_ENABLED, isScreenshotViewBoundsEnabled)
                .putExtra(EXTRA_SCREENSHOT_INTERACTIONS_ENABLED, isScreenshotInteractionsEnabled)
                .putExtra(EXTRA_SCREENSHOT_PREVIEW_LOCATION, screenshotPreviewLocation)
                .putExtra(EXTRA_SCREENSHOT_PREVIEW_SCALE, screenshotPreviewScale)
                .putExtra(EXTRA_SCREENSHOT_STATS_ENABLED, isScreenshotStatsEnabled)
                .putExtra(EXTRA_SCREENSHOT_STATS_LOCATION, screenshotStatsLocation)
                .putExtra(EXTRA_SCREENSHOT_STATS_SCALE, screenshotStatsScale)
                .putExtra(EXTRA_WIREFRAME_HISTORY_OVERRIDE_ENABLED, isWireframeHistoryOverrideEnabled)
                .putExtra(EXTRA_WIREFRAME_HISTORY_SIZE, wireframeHistorySize)
                .putExtra(EXTRA_WIREFRAME_PREVIEW_SKELETONS, isWireframePreviewSkeletonsEnabled)
                .putExtra(EXTRA_WIREFRAME_PREVIEW_VIEW_BOUNDS, isWireframePreviewViewBoundsEnabled)
                .putExtra(EXTRA_WIREFRAME_PREVIEW_LOCATION, wireframePreviewLocation)
                .putExtra(EXTRA_WIREFRAME_PREVIEW_SCALE, wireframePreviewScale)
                .putExtra(EXTRA_WIREFRAME_STATS_ENABLED, isWireframeStatsEnabled)
                .putExtra(EXTRA_WIREFRAME_STATS_LOCATION, wireframeStatsLocation)
                .putExtra(EXTRA_WIREFRAME_STATS_SCALE, wireframeStatsScale)
                .putExtra(EXTRA_WIREFRAME_VIEW_BOUNDS_OVERLAY_ENABLED, isWireframeViewBoundsOverlayEnabled)
                .putExtra(EXTRA_WIREFRAME_SKELETONS_OVERLAY_ENABLED, isWireframeSkeletonsOverlayEnabled)
                .putExtra(EXTRA_WIREFRAME_SKELETONS_OVERLAY_ALPHA, wireframeSkeletonsOverlayAlpha)
                .putExtra(EXTRA_WIREFRAME_LOGGING_CANVAS_CALLS_ENABLED, isWireframeLoggingCanvasCallsEnabled)
                .putExtra(EXTRA_WIREFRAME_SIMULATE_UNKNOWN_VIEWS_ENABLED, isWireframeSimulateUnknownViewsEnabled)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        }
    }
}
