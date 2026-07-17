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

package com.splunk.android.debugger.window

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.view.Choreographer
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import com.splunk.rum.common.utils.extensions.doOnDraw
import com.splunk.rum.common.utils.extensions.doOnLayout
import com.splunk.rum.common.utils.extensions.rootView
import com.splunk.rum.common.utils.extensions.safeSubmit
import com.splunk.rum.common.utils.runOnUiThread
import com.splunk.rum.common.utils.window.WindowCallbackWrapper
import com.splunk.android.debugger.R
import com.splunk.android.debugger.databinding.SldWindowDebugBinding
import com.splunk.android.debugger.drawer.WireframeDrawer
import com.splunk.android.debugger.extension.copy
import com.splunk.android.debugger.extension.getContentPadding
import com.splunk.android.debugger.extension.getKeyboardHeight
import com.splunk.android.debugger.extension.updateLayoutParams
import com.splunk.android.debugger.extension.withDisabledAnimations
import com.splunk.android.debugger.model.Location
import com.splunk.android.debugger.model.Shortcut
import com.splunk.android.debugger.util.AnimationUtils
import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.instrumentation.recording.interactions.extension.isInvisibleForInteractions
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.screenshot.extension.isInvisibleForScreenshot
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot
import com.splunk.android.instrumentation.recording.screenshot.stats.ScreenshotStats
import com.splunk.android.instrumentation.recording.wireframe.extension.isInvisibleForWireframe
import com.splunk.android.instrumentation.recording.wireframe.extension.waitToFinish
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats
import java.util.concurrent.Executors

/* FIXME
 *  - View bounds depth in NewPipe
 *  - Wrong height when opened keyboard in Wikipedia search
 *  - Broken in RTL layout
 *  - Button is not moved down when an Activity crash with opened keyboard
 *  - Window has wrong size when keyboard is opened
 */
internal class DebugWindow {

    private val choreographer = Choreographer.getInstance()
    private val wireframeAsyncUpdater = Executors.newFixedThreadPool(1)

    private var windowManager: WindowManager? = null
    private var viewBinding: SldWindowDebugBinding? = null

    private var wireframeStatsBackup: List<WireframeStats>? = null
    private var wireframeSceneBackup: Wireframe.Frame.Scene? = null

    private var screenshotStatsBackup: List<ScreenshotStats>? = null

    private var isOpenedBackup = false

    var listener: Listener? = null

    var isPreviewRecordingEnabled: Boolean = false
        set(value) {
            field = value

            viewBinding?.root?.isInvisibleForScreenshot = !value
            viewBinding?.root?.isInvisibleForWireframe = !value
        }

    var isRecording = false
        set(value) {
            field = value
            updateRecordingStatus()
        }

    var isInteractionPreviewEnabled = false
        set(value) {
            field = value
            viewBinding?.interactions?.visibility = if (value) View.VISIBLE else View.GONE
        }

    var isScreenshotPreviewEnabled = false
        set(value) {
            field = value
            viewBinding?.screenshotPreview?.visibility = if (value) View.VISIBLE else View.GONE
            viewBinding?.screenshotViewBoundsPreview?.visibility = if (value && isScreenshotViewBoundsEnabled) View.VISIBLE else View.GONE
            viewBinding?.screenshotInteractionsPreview?.visibility = if (value && isScreenshotInteractionsEnabled) View.VISIBLE else View.GONE
        }

    var isScreenshotViewBoundsEnabled = false
        set(value) {
            field = value
            viewBinding?.screenshotViewBoundsPreview?.visibility = if (value && isScreenshotPreviewEnabled) View.VISIBLE else View.GONE
        }

    var isScreenshotInteractionsEnabled = false
        set(value) {
            field = value
            viewBinding?.screenshotInteractionsPreview?.visibility = if (value && isScreenshotPreviewEnabled) View.VISIBLE else View.GONE
        }

    var screenshotPreviewScale = 1f
        set(value) {
            viewBinding?.let {
                placeView(it.screenshotPreview, screenshotPreviewLocation, value)
                placeView(it.screenshotViewBoundsPreview, screenshotPreviewLocation, value)
                placeView(it.screenshotInteractionsPreview, screenshotPreviewLocation, value)
            }
            field = value
        }

    var screenshotPreviewLocation = Location.TOP_LEFT
        set(value) {
            viewBinding?.let {
                placeView(it.screenshotPreview, value, screenshotPreviewScale)
                placeView(it.screenshotViewBoundsPreview, value, screenshotPreviewScale)
                placeView(it.screenshotInteractionsPreview, value, screenshotPreviewScale)
            }
            field = value
        }

    var isScreenshotStatsEnabled = true
        set(value) {
            viewBinding?.screenshotStats?.visibility = if (value) View.VISIBLE else View.INVISIBLE
            field = value
        }

    var screenshotStatsScale = 1f
        set(value) {
            viewBinding?.let { placeView(it.screenshotStats, screenshotStatsLocation, value) }
            field = value
        }

    var screenshotStatsLocation = Location.TOP_RIGHT
        set(value) {
            viewBinding?.let { placeView(it.screenshotStats, value, screenshotStatsScale) }
            field = value
        }

    var isWireframePreviewSkeletonsEnabled = true
        set(value) {
            field = value
            viewBinding?.wireframePreview?.flags = getWireframePreviewFlags()
            viewBinding?.wireframePreview?.visibility = if (value || isWireframePreviewViewBoundsEnabled) View.VISIBLE else View.GONE
        }

    var isWireframePreviewViewBoundsEnabled = false
        set(value) {
            field = value
            viewBinding?.wireframePreview?.flags = getWireframePreviewFlags()
            viewBinding?.wireframePreview?.visibility = if (value || isWireframePreviewSkeletonsEnabled) View.VISIBLE else View.GONE
        }

    var wireframePreviewScale = 1f
        set(value) {
            viewBinding?.let { placeView(it.wireframePreview, wireframePreviewLocation, value) }
            field = value
        }

    var wireframePreviewLocation = Location.TOP_RIGHT
        set(value) {
            viewBinding?.let { placeView(it.wireframePreview, value, wireframePreviewScale) }
            field = value
        }

    var isWireframeStatsEnabled = true
        set(value) {
            viewBinding?.wireframeStats?.visibility = if (value) View.VISIBLE else View.INVISIBLE
            field = value
        }

    var wireframeStatsScale = 1f
        set(value) {
            viewBinding?.let { placeView(it.wireframeStats, wireframeStatsLocation, value) }
            field = value
        }

    var wireframeStatsLocation = Location.TOP_LEFT
        set(value) {
            viewBinding?.let { placeView(it.wireframeStats, value, wireframeStatsScale) }
            field = value
        }

    var isWireframeViewBoundsOverlayEnabled = false
        set(value) {
            viewBinding?.wireframeViewBorderOverlay?.visibility = if (value) View.VISIBLE else View.GONE
            field = value
        }

    var isWireframeSkeletonsOverlayEnabled = false
        set(value) {
            viewBinding?.wireframeSkeletonsOverlay?.visibility = if (value) View.VISIBLE else View.GONE
            field = value
        }

    var wireframeSkeletonsOverlayAlpha = 0.5f
        set(value) {
            viewBinding?.wireframeSkeletonsOverlay?.alpha = value
            field = value
        }

    val rootView: View?
        get() = viewBinding?.root

    fun show(activity: Activity) {
        activity.rootView?.doOnDraw {
            if (viewBinding?.root?.attachedActivity !== activity) {
                hide()
                prepare(activity)

                wireframeStatsBackup = null
            } else if (viewBinding != null)
                return@doOnDraw

            val viewBinding = viewBinding ?: return@doOnDraw
            val layoutParams = createLayoutParams(WindowManager.LayoutParams.MATCH_PARENT)

            updateContentPadding(activity)
            windowManager?.addView(viewBinding.root, layoutParams)
            activity.window.callback = ControlsCallback(activity.window?.callback) // FIXME Use WindowCallbackManager

            choreographer.postFrameCallback(frameCallback)
        }
    }

    fun hide(backup: Boolean = true) {
        choreographer.removeFrameCallback(frameCallback)

        val windowManager = windowManager ?: return
        val viewBinding = viewBinding ?: return

        if (viewBinding.root.parent != null)
            windowManager.removeViewImmediate(viewBinding.root)

        if (backup) {
            wireframeStatsBackup = viewBinding.wireframeStats.getStatsList()
            wireframeSceneBackup = viewBinding.wireframePreview.scene

            screenshotStatsBackup = viewBinding.screenshotStats.getStatsList()

            isOpenedBackup = viewBinding.cover.visibility == View.VISIBLE
        }

        this.viewBinding = null
        this.windowManager = null
    }

    fun updateWireframe(frame: Wireframe.Frame, stats: WireframeStats) {
        val viewBinding = viewBinding ?: return
        val scene = frame.scenes.first()

        viewBinding.wireframeStats.addStats(stats)

        wireframeAsyncUpdater.safeSubmit {
            frame.waitToFinish()

            runOnUiThread {
                viewBinding.screenshotViewBoundsPreview.scene = scene

                viewBinding.wireframePreview.scene = scene
                viewBinding.wireframeViewBorderOverlay.scene = scene
                viewBinding.wireframeSkeletonsOverlay.scene = scene

                viewBinding.interactions.wireframeScene = scene
                viewBinding.screenshotInteractionsPreview.wireframeScene = scene
            }
        }
    }

    fun updateScreenshot(screenshot: Screenshot, stats: ScreenshotStats) {
        val viewBinding = viewBinding ?: return

        viewBinding.screenshotPreview.screenshot = screenshot
        viewBinding.screenshotStats.addStats(stats)
    }

    fun updateInteractions(interactions: List<Interaction>) {
        val viewBinding = viewBinding ?: return

        viewBinding.interactions.interactions = interactions
        viewBinding.wireframePreview.interactions = interactions
        viewBinding.screenshotPreview.interactions = interactions
        viewBinding.screenshotInteractionsPreview.interactions = interactions
    }

    fun isAttachedToActivity(activity: Activity): Boolean {
        return viewBinding?.root?.attachedActivity === activity
    }

    private fun prepare(activity: Activity) {
        val inflater = LayoutInflater.from(activity)
        val viewBinding = SldWindowDebugBinding.inflate(inflater)

        viewBinding.root.attachedActivity = activity
        viewBinding.root.isInvisibleForWireframe = !isPreviewRecordingEnabled
        viewBinding.root.isInvisibleForScreenshot = !isPreviewRecordingEnabled
        viewBinding.root.isInvisibleForInteractions = !isPreviewRecordingEnabled

        val onLayoutChangeListener = OnLayoutChangeListener()
        viewBinding.wireframePreview.addOnLayoutChangeListener(onLayoutChangeListener)
        viewBinding.wireframeStats.addOnLayoutChangeListener(onLayoutChangeListener)

        val onClickListener = OnClickListener()
        viewBinding.cover.setOnClickListener(onClickListener)
        viewBinding.button.setOnClickListener(onClickListener)
        viewBinding.recording.setOnClickListener(onClickListener)
        viewBinding.screenshotDetail.setOnClickListener(onClickListener)
        viewBinding.wireframeDetail.setOnClickListener(onClickListener)
        viewBinding.log.setOnClickListener(onClickListener)
        viewBinding.settings.setOnClickListener(onClickListener)
        viewBinding.sdk.setOnClickListener(onClickListener)

        val onLongClickListener = OnLongClickListener()
        viewBinding.button.setOnLongClickListener(onLongClickListener)
        viewBinding.recording.setOnLongClickListener(onLongClickListener)
        viewBinding.screenshotDetail.setOnLongClickListener(onLongClickListener)
        viewBinding.wireframeDetail.setOnLongClickListener(onLongClickListener)
        viewBinding.log.setOnLongClickListener(onLongClickListener)
        viewBinding.settings.setOnLongClickListener(onLongClickListener)
        viewBinding.sdk.setOnLongClickListener(onLongClickListener)

        updateRecordingStatus()

        viewBinding.interactions.visibility = if (isInteractionPreviewEnabled) View.VISIBLE else View.GONE
        viewBinding.screenshotPreview.visibility = if (isScreenshotPreviewEnabled) View.VISIBLE else View.GONE
        viewBinding.screenshotViewBoundsPreview.visibility = if (isScreenshotViewBoundsEnabled && isScreenshotPreviewEnabled) View.VISIBLE else View.GONE
        viewBinding.screenshotInteractionsPreview.visibility = if (isScreenshotInteractionsEnabled && isScreenshotPreviewEnabled) View.VISIBLE else View.GONE
        viewBinding.screenshotStats.visibility = if (isScreenshotStatsEnabled) View.VISIBLE else View.GONE
        viewBinding.wireframePreview.visibility = if (isWireframePreviewSkeletonsEnabled || isWireframePreviewViewBoundsEnabled) View.VISIBLE else View.GONE
        viewBinding.wireframeStats.visibility = if (isWireframeStatsEnabled) View.VISIBLE else View.GONE
        viewBinding.wireframeViewBorderOverlay.visibility = if (isWireframeViewBoundsOverlayEnabled) View.VISIBLE else View.GONE
        viewBinding.wireframeSkeletonsOverlay.visibility = if (isWireframeSkeletonsOverlayEnabled) View.VISIBLE else View.GONE
        viewBinding.wireframeSkeletonsOverlay.alpha = wireframeSkeletonsOverlayAlpha

        placeView(viewBinding.screenshotPreview, screenshotPreviewLocation, screenshotPreviewScale)
        placeView(viewBinding.screenshotViewBoundsPreview, screenshotPreviewLocation, screenshotPreviewScale)
        placeView(viewBinding.screenshotInteractionsPreview, screenshotPreviewLocation, screenshotPreviewScale)
        placeView(viewBinding.screenshotStats, screenshotStatsLocation, screenshotStatsScale)
        placeView(viewBinding.wireframePreview, wireframePreviewLocation, wireframePreviewScale)
        placeView(viewBinding.wireframeStats, wireframeStatsLocation, wireframeStatsScale)

        wireframeStatsBackup?.let { viewBinding.wireframeStats.setStatsList(it) }
        wireframeSceneBackup?.let { viewBinding.wireframePreview.scene = it }

        screenshotStatsBackup?.let { viewBinding.screenshotStats.setStatsList(it) }

        if (isOpenedBackup)
            viewBinding.root.withDisabledAnimations {
                viewBinding.wheelContainer.visibility = View.VISIBLE
                viewBinding.cover.visibility = View.VISIBLE
            }

        viewBinding.wireframePreview.flags = getWireframePreviewFlags()

        windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        this.viewBinding = viewBinding
    }

    private fun updateRecordingStatus() {
        val viewBinding = viewBinding ?: return
        val recordingStringRes = if (isRecording) R.string.sld_menu_rendering_stop else R.string.sld_menu_rendering_start

        viewBinding.recording.setText(recordingStringRes)
        viewBinding.recording.isActivated = isRecording
    }

    private fun createLayoutParams(height: Int): WindowManager.LayoutParams {
        val type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        val flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        val layoutParams = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, height, type, flags, PixelFormat.TRANSPARENT)
        layoutParams.gravity = Gravity.TOP

        return layoutParams
    }

    private fun updateContentPadding(activity: Activity) {
        val padding = activity.getContentPadding() ?: return
        viewBinding?.container?.setPadding(padding.left, padding.top, padding.right, padding.bottom)
    }

    private fun placeView(view: View, location: Location, scale: Float) {
        view.updateLayoutParams<FrameLayout.LayoutParams> {
            it.gravity = location.toGravity()
        }

        if (view.width == 0 || view.height == 0) {
            view.doOnLayout { placeView(view, location, scale) }
            return
        }

        val pivotX: Float
        val pivotY: Float

        when (location) {
            Location.TOP_LEFT -> {
                pivotX = 0f
                pivotY = 0f
            }
            Location.TOP_RIGHT -> {
                pivotX = view.width.toFloat()
                pivotY = 0f
            }
            Location.BOTTOM_LEFT -> {
                pivotX = 0f
                pivotY = view.height.toFloat()
            }
            Location.BOTTOM_RIGHT -> {
                pivotX = view.width.toFloat()
                pivotY = view.height.toFloat()
            }
        }

        view.pivotX = pivotX
        view.pivotY = pivotY
        view.scaleX = scale
        view.scaleY = scale
    }

    private fun getWireframePreviewFlags(): Int {
        var flags = 0

        if (isWireframePreviewSkeletonsEnabled)
            flags = flags or WireframeDrawer.FLAG_SKELETONS

        if (isWireframePreviewViewBoundsEnabled)
            flags = flags or WireframeDrawer.FLAG_VIEW_BORDERS

        return flags
    }

    private fun toggleWheelVisibility() {
        val viewBinding = viewBinding ?: return
        val isVisible = viewBinding.wheelContainer.visibility != View.VISIBLE
        val visibility = if (isVisible) View.VISIBLE else View.GONE

        AnimationUtils.reveal(viewBinding.wheelContainer, viewBinding.button, isVisible)
        viewBinding.cover.visibility = visibility

        viewBinding.wireframeDetail.isEnabled = FrameCapturer.frameHolder.getLastWireframeFrame() != null
        viewBinding.screenshotDetail.isEnabled = FrameCapturer.frameHolder.getLastScreenshot() != null

        updateRecordingStatus()

        listener?.onWheelVisibilityChanged(viewBinding.root.context, isVisible)
    }

    private fun toggleRecording(context: Context) {
        listener?.toggleRecording(context)
    }

    private fun showScreenshotDetail(context: Context) {
        listener?.showScreenshotDetail(context)
        toggleWheelVisibility()
    }

    private fun showWireframeDetail(context: Context) {
        listener?.showWireframeDetail(context)
        toggleWheelVisibility()
    }

    private fun openSettings(context: Context) {
        listener?.openSettings(context)
    }

    private fun openSdk(context: Context) {
        listener?.openSdk(context)
    }

    private fun showLog(context: Context) {
        listener?.showLog(context)
    }

    private fun updateShortcut(context: Context, shortcut: Shortcut) {
        listener?.updateShortcut(context, shortcut)

        Toast.makeText(context, R.string.sld_menu_shortcut_updated, Toast.LENGTH_SHORT).show()
        toggleWheelVisibility()
    }

    private var View.attachedActivity: Activity?
        get() = getTag(R.id.sld_tag_activity) as? Activity
        set(value) = setTag(R.id.sld_tag_activity, value)

    private val frameCallback = object : Choreographer.FrameCallback {

        private var lastKeyboardHeight = 0

        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)

            val rootView = viewBinding?.root ?: return
            val activity = rootView.attachedActivity ?: return
            val keyboardHeight = activity.getKeyboardHeight()

            if (keyboardHeight != lastKeyboardHeight) {
                val height = if (keyboardHeight > 0) rootView.height + lastKeyboardHeight - keyboardHeight else WindowManager.LayoutParams.MATCH_PARENT
                windowManager?.updateViewLayout(rootView, createLayoutParams(height))
                lastKeyboardHeight = keyboardHeight
            }
        }
    }

    private inner class OnLayoutChangeListener : View.OnLayoutChangeListener {
        override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            val viewBinding = viewBinding ?: return

            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom)
                when (view.id) {
                    viewBinding.wireframePreview.id ->
                        placeView(view, wireframePreviewLocation, wireframePreviewScale)
                    viewBinding.wireframeStats.id ->
                        placeView(view, wireframeStatsLocation, wireframeStatsScale)
                    viewBinding.screenshotPreview.id -> {
                        placeView(view, screenshotPreviewLocation, screenshotPreviewScale)
                        placeView(viewBinding.screenshotViewBoundsPreview, screenshotPreviewLocation, screenshotPreviewScale)
                        placeView(viewBinding.screenshotInteractionsPreview, screenshotPreviewLocation, screenshotPreviewScale)
                    }
                }
        }
    }

    private inner class OnClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            val viewBinding = viewBinding ?: return

            when (view.id) {
                viewBinding.cover.id, viewBinding.button.id ->
                    toggleWheelVisibility()
                viewBinding.recording.id ->
                    toggleRecording(view.context)
                viewBinding.screenshotDetail.id ->
                    showScreenshotDetail(view.context)
                viewBinding.wireframeDetail.id ->
                    showWireframeDetail(view.context)
                viewBinding.settings.id ->
                    openSettings(view.context)
                viewBinding.sdk.id ->
                    openSdk(view.context)
                viewBinding.log.id ->
                    showLog(view.context)
            }
        }
    }

    private inner class OnLongClickListener : View.OnLongClickListener {
        override fun onLongClick(view: View): Boolean {
            val viewBinding = viewBinding ?: return false

            when (view.id) {
                viewBinding.button.id ->
                    listener?.executeShortcut(view.context)
                viewBinding.recording.id ->
                    updateShortcut(view.context, Shortcut.TOGGLE_RECORDING)
                viewBinding.wireframeDetail.id ->
                    updateShortcut(view.context, Shortcut.WIREFRAME_DETAIL)
                viewBinding.screenshotDetail.id ->
                    updateShortcut(view.context, Shortcut.SCREENSHOT_DETAIL)
                viewBinding.log.id ->
                    updateShortcut(view.context, Shortcut.SHOW_LOG)
            }

            return true
        }
    }

    private inner class ControlsCallback(callback: Window.Callback?) : WindowCallbackWrapper(callback) {

        private val location = IntArray(2)

        override fun dispatchKeyEvent(event: KeyEvent): Boolean {
            if (event.keyCode == KeyEvent.KEYCODE_BACK && viewBinding?.wheelContainer?.visibility == View.VISIBLE) {
                if (event.action == KeyEvent.ACTION_UP)
                    toggleWheelVisibility()

                return true
            }

            return super.dispatchKeyEvent(event)
        }

        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            val viewBinding = viewBinding ?: return super.dispatchTouchEvent(event)
            viewBinding.root.getLocationOnScreen(location)

            val x = event.rawX - location[0]
            val y = event.rawY - location[1]
            val customEvent = event.copy(x, y)

            return viewBinding.root.dispatchTouchEvent(customEvent) || super.dispatchTouchEvent(event)
        }
    }

    interface Listener {
        fun onWheelVisibilityChanged(context: Context, isVisible: Boolean)
        fun toggleRecording(context: Context)
        fun showWireframeDetail(context: Context)
        fun showScreenshotDetail(context: Context)
        fun showLog(context: Context)
        fun openSettings(context: Context)
        fun openSdk(context: Context)
        fun executeShortcut(context: Context)
        fun updateShortcut(context: Context, shortcut: Shortcut)
    }
}
