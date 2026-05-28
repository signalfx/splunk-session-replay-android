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

package com.splunk.android.debugger.screen.wireframe.content

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.splunk.android.common.utils.runOnBackgroundThread
import com.splunk.android.common.utils.runOnUiThread
import com.splunk.android.debugger.R
import com.splunk.android.debugger.databinding.SldFragmentWireframeModelBinding
import com.splunk.android.debugger.drawer.WireframeDrawer
import com.splunk.android.debugger.screen.wireframe.WireframeFragment
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import java.io.File
import java.io.FileOutputStream

internal class ModelFragment : WireframeFragment() {

    private var viewBinding: SldFragmentWireframeModelBinding? = null

    private val interactions: List<Interaction> = Companion.interactions ?: error("start() not called")

    var wireframeScene: Wireframe.Frame.Scene = Companion.wireframeScene ?: error("start() not called")
        set(value) {
            viewBinding?.wireframe?.scene = value
            viewBinding?.interactions?.timestampReference = value.time
            field = value
        }

    override var isControlsVisible: Boolean
        get() = viewBinding?.buttonsContainer?.visibility == View.VISIBLE
        set(value) {
            viewBinding?.buttonsContainer?.visibility = if (value) View.VISIBLE else View.GONE
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = SldFragmentWireframeModelBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewBinding = viewBinding ?: return

        viewBinding.viewBorder.setOnClickListener(onCheckClickListener)
        viewBinding.skeletons.setOnClickListener(onCheckClickListener)
        viewBinding.saveFrame.setOnClickListener(onClickListener)

        viewBinding.wireframe.scene = wireframeScene
        viewBinding.interactions.interactions = interactions
        viewBinding.interactions.timestampReference = wireframeScene.time

        updateSelections()
    }

    override fun onDestroyView() {
        viewBinding = null
        super.onDestroyView()
    }

    private fun updateSelections() {
        val viewBinding = viewBinding ?: return
        val flags = viewBinding.wireframe.flags

        viewBinding.skeletons.isChecked = flags and WireframeDrawer.FLAG_SKELETONS == WireframeDrawer.FLAG_SKELETONS
        viewBinding.viewBorder.isChecked = flags and WireframeDrawer.FLAG_VIEW_BORDERS == WireframeDrawer.FLAG_VIEW_BORDERS
    }

    private fun saveBitmap() {
        val viewBinding = viewBinding ?: return
        val context = viewBinding.root.context

        val directory = File(context.getExternalFilesDir(null), "Cisco")
        directory.mkdirs()

        runOnBackgroundThread("ModelFragment::saveBitmap") {
            val file = File(directory, "${wireframeScene.time}.wireframe.png")
            val bitmap = WireframeDrawer.draw(wireframeScene, viewBinding.wireframe.flags)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(file))

            runOnUiThread { Toast.makeText(context, context.getString(R.string.sld_wireframe_saved, file), Toast.LENGTH_LONG).show() }
        }
    }

    private val onCheckClickListener = View.OnClickListener {
        val viewBinding = viewBinding ?: return@OnClickListener

        val flag = when (it.id) {
            viewBinding.viewBorder.id ->
                WireframeDrawer.FLAG_VIEW_BORDERS
            viewBinding.skeletons.id ->
                WireframeDrawer.FLAG_SKELETONS
            else ->
                return@OnClickListener
        }

        val flags = viewBinding.wireframe.flags

        viewBinding.wireframe.flags = if (flags and flag == flag)
            flags and flag.inv()
        else
            flags or flag

        updateSelections()
    }

    private val onClickListener = View.OnClickListener {
        val viewBinding = viewBinding ?: return@OnClickListener

        when (it.id) {
            viewBinding.saveFrame.id ->
                saveBitmap()
        }
    }

    companion object {

        private var wireframeScene: Wireframe.Frame.Scene? = null
        private var interactions: List<Interaction>? = null

        fun create(wireframeScene: Wireframe.Frame.Scene, interactions: List<Interaction>): ModelFragment {
            this.wireframeScene = wireframeScene
            this.interactions = interactions

            return ModelFragment()
        }
    }
}
