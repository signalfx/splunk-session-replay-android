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

package com.splunk.android.debugger.screen.wireframe

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.splunk.rum.common.utils.extensions.contentView
import com.splunk.rum.common.utils.runOnBackgroundThread
import com.splunk.android.debugger.R
import com.splunk.android.debugger.databinding.SldActivityWireframeBinding
import com.splunk.android.debugger.drawer.WireframeDrawer
import com.splunk.android.debugger.encoder.VideoEncoder
import com.splunk.android.debugger.extension.applicationName
import com.splunk.android.debugger.extension.createFileInDownloadDirectory
import com.splunk.android.debugger.extension.setDecorFitsSystemWindowsCompat
import com.splunk.android.debugger.screen.wireframe.content.JsonFragment
import com.splunk.android.debugger.screen.wireframe.content.ModelFragment
import com.splunk.android.debugger.screen.wireframe.content.UnknownFragment
import com.splunk.android.debugger.util.DebugToast
import com.splunk.android.debugger.view.TimelineView
import com.splunk.android.instrumentation.recording.interactions.extension.isInvisibleForInteractions
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.screenshot.extension.isInvisibleForScreenshot
import com.splunk.android.instrumentation.recording.wireframe.extension.isInvisibleForWireframe
import com.splunk.android.instrumentation.recording.wireframe.model.ClassDefinition
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.min

// FIXME Toasts broke history
// FIXME Navigation bar

internal class WireframeActivity : FragmentActivity() {

    private lateinit var viewBinding: SldActivityWireframeBinding

    private val wireframeFrames: List<Wireframe.Frame> = Companion.wireframeFrames ?: error("start() not called")
    private val unknownClasses: List<ClassDefinition> = Companion.unknownClasses ?: error("start() not called")
    private val interactions: List<Interaction> = Companion.interactions ?: error("start() not called")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setDecorFitsSystemWindowsCompat(false)
        window.statusBarColor = Color.TRANSPARENT

        viewBinding = SldActivityWireframeBinding.inflate(layoutInflater, contentView, true)

        viewBinding.container.setOnClickListener(onClickListener)
        viewBinding.model.setOnClickListener(onClickListener)
        viewBinding.json.setOnClickListener(onClickListener)
        viewBinding.unknownClasses.setOnClickListener(onClickListener)
        viewBinding.timeline.listener = timelineListener

        viewBinding.timeline.max = wireframeFrames.lastIndex
        viewBinding.timeline.progress = wireframeFrames.lastIndex
        viewBinding.timeline.visibility = if (wireframeFrames.size < 2) View.GONE else View.VISIBLE

        if (savedInstanceState == null) {
            viewBinding.buttonsGroup.visibility = View.GONE
            navigateToModel()
        }

        isInvisibleForWireframe = true
        isInvisibleForScreenshot = true
        isInvisibleForInteractions = true
    }

    private fun setContent(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(viewBinding.container.id, fragment)
            .commitNow()

        val visible = viewBinding.buttonsGroup.visibility == View.VISIBLE
        setContentControlsVisible(visible)
    }

    private fun toggleButtonsVisibility() {
        val visible = viewBinding.buttonsGroup.visibility == View.GONE
        viewBinding.buttonsGroup.visibility = if (visible) View.VISIBLE else View.GONE

        setContentControlsVisible(visible)
    }

    private fun setContentControlsVisible(visible: Boolean) {
        val topFragment = supportFragmentManager.fragments.lastOrNull() as? WireframeFragment
        topFragment?.isControlsVisible = visible
    }

    private fun navigateToModel() {
        if (supportFragmentManager.fragments.lastOrNull() !is ModelFragment) {
            val index = viewBinding.timeline.progress
            val scene = wireframeFrames[index].scenes.first()
            setContent(ModelFragment.create(scene, interactions))
            updateButtons()
        }
    }

    private fun navigateToJson() {
        if (supportFragmentManager.fragments.lastOrNull() !is JsonFragment) {
            val index = viewBinding.timeline.progress
            val scene = wireframeFrames[index].scenes.first()
            setContent(JsonFragment.create(scene))
            updateButtons()
        }
    }

    private fun navigateToUnknownClasses() {
        if (supportFragmentManager.fragments.lastOrNull() !is UnknownFragment) {
            setContent(UnknownFragment.create(unknownClasses))
            updateButtons()
        }
    }

    private fun updateButtons() {
        viewBinding.model.isEnabled = true
        viewBinding.json.isEnabled = true
        viewBinding.unknownClasses.isEnabled = true

        when (supportFragmentManager.fragments.lastOrNull()) {
            is ModelFragment ->
                viewBinding.model.isEnabled = false
            is JsonFragment ->
                viewBinding.json.isEnabled = false
            is UnknownFragment ->
                viewBinding.unknownClasses.isEnabled = false
        }
    }

    private fun updateData(index: Int) {
        val scene = wireframeFrames[index].scenes.first()

        for (fragment in supportFragmentManager.fragments)
            when (fragment) {
                is ModelFragment ->
                    fragment.wireframeScene = scene
                is JsonFragment ->
                    fragment.wireframeScene = scene
            }
    }

    private fun saveVideo() {
        val firstScene = wireframeFrames[viewBinding.timeline.markA].scenes.first()
        val width = firstScene.rect.width()
        val height = firstScene.rect.height()

        val begin = viewBinding.timeline.markA
        val end = viewBinding.timeline.markB

        var minInterval = Long.MAX_VALUE

        for (i in begin..end) {
            val scene = wireframeFrames[i].scenes.first()
            val nextScene = wireframeFrames.getOrNull(i + 1)?.scenes?.first()

            if (scene.rect.width() != width || scene.rect.height() != height) {
                DebugToast.showText(this, getString(R.string.sld_wireframe_video_error_different_size))
                return
            }

            if (nextScene != null)
                minInterval = min(nextScene.time - scene.time, minInterval)
        }

        val frameRate = ceil(1000f / minInterval).toInt()

        val directory = File(getExternalFilesDir(null), "Cisco")
        val file = File(directory, "$applicationName-${firstScene.time}.wireframe.mp4")
        directory.mkdirs()

        viewBinding.timeline.isSaveButtonEnabled = false

        val encoder = VideoEncoder()
        encoder.listener = videoEncoderListener
        encoder.start(file, width, height, frameRate)

        runOnBackgroundThread("WireframeActivity.saveVideo") {
            for (i in begin..end) {
                val scene = wireframeFrames[i].scenes.first()
                val bitmap = WireframeDrawer.draw(scene)
                encoder.addFrame(bitmap, scene.time)
            }

            encoder.stop()
        }
    }

    private val onClickListener = View.OnClickListener {
        when (it.id) {
            viewBinding.container.id ->
                toggleButtonsVisibility()
            viewBinding.model.id ->
                navigateToModel()
            viewBinding.json.id ->
                navigateToJson()
            viewBinding.unknownClasses.id ->
                navigateToUnknownClasses()
        }
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
                DebugToast.showText(this@WireframeActivity, getString(R.string.sld_wireframe_video_saved, finalFile))
                viewBinding.timeline.isSaveButtonEnabled = true
            }
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
            val time = wireframeFrames[progress].scenes.first().time
            return SimpleDateFormat("mm:ss.SSS", Locale.getDefault()).format(time)
        }
    }

    companion object {

        private var wireframeFrames: List<Wireframe.Frame>? = null
        private var unknownClasses: List<ClassDefinition>? = null
        private var interactions: List<Interaction>? = null

        fun start(context: Context, wireframeFrames: List<Wireframe.Frame>, unknownClasses: List<ClassDefinition>, interactions: List<Interaction>) {
            this.wireframeFrames = wireframeFrames
            this.unknownClasses = unknownClasses
            this.interactions = interactions

            val intent = Intent(context, WireframeActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(intent)
        }
    }
}
