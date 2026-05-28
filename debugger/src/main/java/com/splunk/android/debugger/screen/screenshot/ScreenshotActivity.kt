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

package com.splunk.android.debugger.screen.screenshot

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.splunk.android.common.utils.extensions.contentView
import com.splunk.android.common.utils.runOnBackgroundThread
import com.splunk.android.debugger.R
import com.splunk.android.debugger.databinding.SldActivityScreenshotBinding
import com.splunk.android.debugger.encoder.VideoEncoder
import com.splunk.android.debugger.extension.applicationName
import com.splunk.android.debugger.extension.createFileInDownloadDirectory
import com.splunk.android.debugger.extension.setDecorFitsSystemWindowsCompat
import com.splunk.android.debugger.util.DebugToast
import com.splunk.android.debugger.view.TimelineView
import com.splunk.android.instrumentation.recording.interactions.extension.isInvisibleForInteractions
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.screenshot.extension.isInvisibleForScreenshot
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot
import com.splunk.android.instrumentation.recording.wireframe.extension.isInvisibleForWireframe
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

// FIXME Toasts broke history
// FIXME Navigation bar

internal class ScreenshotActivity : FragmentActivity() {

    private lateinit var viewBinding: SldActivityScreenshotBinding

    private val screenshots: List<Screenshot> = Companion.screenshots ?: error("start() not called")
    private val wireframeFrames: List<Wireframe.Frame> = Companion.wireframeFrames ?: error("start() not called")
    private val interactions: List<Interaction> = Companion.interactions ?: error("start() not called")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setDecorFitsSystemWindowsCompat(false)
        window.statusBarColor = Color.TRANSPARENT

        viewBinding = SldActivityScreenshotBinding.inflate(layoutInflater, contentView, true)

        viewBinding.timeline.max = screenshots.lastIndex
        viewBinding.timeline.progress = screenshots.lastIndex

        viewBinding.interactions.interactions = interactions

        viewBinding.touchDispatcher.setOnClickListener(onClickListener)
        viewBinding.viewBoundsButton.setOnClickListener(onClickListener)
        viewBinding.saveScreenshot.setOnClickListener(onClickListener)
        viewBinding.timeline.listener = timelineListener

        if (savedInstanceState == null)
            viewBinding.timeline.visibility = View.GONE

        updateData(screenshots.lastIndex)

        isInvisibleForWireframe = true
        isInvisibleForScreenshot = true
        isInvisibleForInteractions = true
    }

    private fun swapControlsVisibility() {
        viewBinding.buttonsContainer.visibility = if (viewBinding.buttonsContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        viewBinding.timeline.visibility = if (viewBinding.timeline.visibility == View.GONE && screenshots.size > 1) View.VISIBLE else View.GONE
    }

    private fun swapViewBoundsVisibility() {
        val visible = viewBinding.viewBounds.visibility == View.GONE
        viewBinding.viewBounds.visibility = if (visible) View.VISIBLE else View.GONE
        viewBinding.viewBoundsButton.isChecked = visible
    }

    private fun saveBitmap() {
        val index = viewBinding.timeline.progress
        val screenshot = screenshots[index]

        val directory = File(getExternalFilesDir(null), "Cisco")
        directory.mkdirs()

        runOnBackgroundThread("ScreenshotActivity::saveBitmap") {
            val file = File(directory, "${screenshot.time}.screenshot.png")
            screenshot.bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(file))

            runOnUiThread { DebugToast.showText(this, getString(R.string.sld_screenshot_image_saved, file)) }
        }
    }

    private fun saveVideo() {
        val firstScreenshot = screenshots[viewBinding.timeline.markA]
        val width = firstScreenshot.bitmap.width
        val height = firstScreenshot.bitmap.height

        val begin = viewBinding.timeline.markA
        val end = viewBinding.timeline.markB

        var minInterval = Long.MAX_VALUE

        for (i in begin..end) {
            val screenshot = screenshots[i]
            val nextScreenshot = screenshots.getOrNull(i + 1)

            if (screenshot.bitmap.width != width || screenshot.bitmap.height != height) { // FIXME Screen orientation, split screen, ...
                DebugToast.showText(this, getString(R.string.sld_screenshot_video_error_different_size))
                return
            }

            if (nextScreenshot != null)
                minInterval = min(nextScreenshot.time - screenshot.time, minInterval)
        }

        val frameRate = ceil(1000f / minInterval).toInt()

        val directory = File(getExternalFilesDir(null), "Cisco")
        val fileName = "$applicationName-${firstScreenshot.time}.screenshot.mp4"
        val file = File(directory, fileName)
        directory.mkdirs()

        viewBinding.timeline.isSaveButtonEnabled = false

        val encoder = VideoEncoder()
        encoder.listener = videoEncoderListener
        encoder.start(file, width, height, frameRate)

        for (i in begin..end) {
            val screenshot = screenshots[i]
            encoder.addFrame(screenshot.bitmap, screenshot.time)
        }

        encoder.stop()
    }

    private fun updateData(index: Int) {
        val screenshot = screenshots[index]
        val frame = wireframeFrames.find { it.scenes.first().time == screenshot.time }

        viewBinding.screenshot.screenshot = screenshot
        viewBinding.viewBounds.scene = frame?.scenes?.first()
        viewBinding.interactions.timestampReference = screenshot.time
    }

    private val videoEncoderListener = object : VideoEncoder.Listener {
        override fun onCompleted(file: File) {
            val outputStream = createFileInDownloadDirectory(file.name)

            val finalFile: File = if (outputStream != null) {
                file.inputStream().copyTo(outputStream)
                outputStream.close()
                file.delete()

                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), file.name)
            } else
                file

            runOnUiThread {
                DebugToast.showText(this@ScreenshotActivity, getString(R.string.sld_screenshot_video_saved, finalFile))
                viewBinding.timeline.isSaveButtonEnabled = true
            }
        }
    }

    private val onClickListener = View.OnClickListener {
        when (it.id) {
            viewBinding.touchDispatcher.id ->
                swapControlsVisibility()
            viewBinding.viewBoundsButton.id ->
                swapViewBoundsVisibility()
            viewBinding.saveScreenshot.id ->
                saveBitmap()
        }
    }

    private val timelineListener = object : TimelineView.Listener {

        override fun onProgressChanged(view: TimelineView, progress: Int) {
            updateData(progress)
        }

        override fun onSaveClicked(view: TimelineView) {
            saveVideo()
        }

        override fun formatProgress(view: TimelineView, progress: Int): String {
            val time = screenshots[progress].time
            return SimpleDateFormat("mm:ss.SSS", Locale.getDefault()).format(time)
        }
    }

    companion object {

        private var screenshots: List<Screenshot>? = null
        private var wireframeFrames: List<Wireframe.Frame>? = null
        private var interactions: List<Interaction>? = null

        fun start(context: Context, screenshots: List<Screenshot>, wireframeFrames: List<Wireframe.Frame>, interactions: List<Interaction>) {
            this.screenshots = screenshots
            this.wireframeFrames = wireframeFrames
            this.interactions = interactions

            val intent = Intent(context, ScreenshotActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        }
    }
}
