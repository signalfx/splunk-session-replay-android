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

package com.splunk.android.instrumentation.recording.interactions.consumer

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import com.splunk.android.common.utils.extensions.activity
import com.splunk.android.common.utils.extensions.rootView
import com.splunk.android.instrumentation.recording.interactions.EventConsumer
import com.splunk.android.instrumentation.recording.interactions.OnInteractionListener
import com.splunk.android.instrumentation.recording.interactions.R
import com.splunk.android.instrumentation.recording.interactions.extension.createElementNodeInfoPath
import com.splunk.android.instrumentation.recording.interactions.extension.resolve
import com.splunk.android.instrumentation.recording.interactions.extension.trimEnd
import com.splunk.android.instrumentation.recording.interactions.model.ElementNode
import com.splunk.android.instrumentation.recording.interactions.model.ElementNodeInfo
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.interactions.model.LegacyData
import com.splunk.android.instrumentation.recording.interactions.util.InteractionIdProvider
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import kotlin.reflect.KClass

// TODO Compose focus
// FIXME Open Wireframe screen > Focus EditText > Open AlertDialog > Focus EditText > close - Focus event is missing
// FIXME Open Focus screen > Focus NumberPicker > Tap out of Views (new Focus screen is opened) > Go back - NumberPicker is not focused (elementNodeInfo was not trimmed)

internal class FocusConsumer(listener: OnInteractionListener) : EventConsumer(listener) {

    private var wireframeFrame: Wireframe.Frame? = null
    private var activeFocusData: ActiveFocusData? = null

    val focusableClasses: MutableSet<KClass<out View>> = hashSetOf(EditText::class)

    override fun onWireframeUpdated(frame: Wireframe.Frame) {
        wireframeFrame = frame

        val activeFocusData = activeFocusData ?: return
        val targetElementPath = activeFocusData.elementNodeInfo.resolve(frame)

        if (targetElementPath.isNotEmpty() && !areTargetElementPathsTheSame(targetElementPath, activeFocusData.targetElementPath))
            onFocusUpdated(targetElementPath, activeFocusData.legacyData)
        else if (targetElementPath.isEmpty() && activeFocusData.targetElementPath.isNotEmpty())
            onFocusLost()

        activeFocusData.targetElementPath = targetElementPath
    }

    override fun onRootViewAdded(rootView: View) {
        val onGlobalFocusChangeListener = OnGlobalFocusChangeListener(rootView)
        rootView.onGlobalFocusChangeListener = onGlobalFocusChangeListener
        rootView.viewTreeObserver.addOnGlobalFocusChangeListener(onGlobalFocusChangeListener)

        val focusedView = rootView.findFocus() ?: return
        onFocusChanged(rootView, focusedView)
    }

    override fun onRootViewRemoved(rootView: View) {
        val onGlobalFocusChangeListener = rootView.onGlobalFocusChangeListener ?: return
        rootView.viewTreeObserver.removeOnGlobalFocusChangeListener(onGlobalFocusChangeListener)

        if (rootView.activity?.isFinishing == false && rootView.findFocus() != null)
            onFocusChanged(rootView, null)
    }

    override fun onActivityResumed(activity: Activity) {
        val rootView = activity.rootView ?: return
        val focusedView = rootView.findFocus() ?: return

        onFocusChanged(rootView, focusedView)
    }

    override fun onActivityPaused(activity: Activity) {
        val rootView = activity.rootView ?: return

        onFocusChanged(rootView, null)
    }

    private fun areTargetElementPathsTheSame(path1: List<ElementNode>, path2: List<ElementNode>): Boolean {
        if (path1.size != path2.size)
            return false

        for (i in path1.indices) {
            val element1 = path1[i]
            val element2 = path2[i]

            if (element1.fragmentTag != element2.fragmentTag)
                return false

            if (element1.positionInList != element2.positionInList)
                return false

            if (element1.view.id != element2.view.id || element1.view.identity != element2.view.identity)
                return false
        }

        return true
    }

    private fun onFocusChanged(rootView: View, newFocus: View?) {
        val wireframeFrame = wireframeFrame

        if (newFocus != null && wireframeFrame != null && focusableClasses.any { it.java.isAssignableFrom(newFocus::class.java) }) {
            val legacyData = LegacyData.create(rootView, newFocus)
            val elementNodeInfo = newFocus.createElementNodeInfoPath()
            val targetElementPath = elementNodeInfo.resolve(wireframeFrame, true)

            elementNodeInfo.trimEnd(targetElementPath)
            activeFocusData = ActiveFocusData(elementNodeInfo, legacyData, targetElementPath)

            if (targetElementPath.isNotEmpty())
                onFocusUpdated(targetElementPath, legacyData)
        } else if (activeFocusData != null) {
            activeFocusData = null
            onFocusLost()
        }
    }

    private fun onFocusUpdated(targetElementPath: List<ElementNode>, legacyData: LegacyData) {
        val id = InteractionIdProvider.next()
        val timestamp = System.currentTimeMillis()
        val interaction = Interaction.Focus(id, timestamp, targetElementPath)

        listener.onInteraction(interaction, legacyData)
    }

    private fun onFocusLost() {
        val id = InteractionIdProvider.next()
        val timestamp = System.currentTimeMillis()
        val interaction = Interaction.Focus(id, timestamp, null)

        listener.onInteraction(interaction)
    }

    private var View.onGlobalFocusChangeListener: OnGlobalFocusChangeListener?
        get() = getTag(R.id.sl_tag_focus_listener) as? OnGlobalFocusChangeListener
        set(value) = setTag(R.id.sl_tag_focus_listener, value)

    private inner class OnGlobalFocusChangeListener(
        private val rootView: View
    ) : ViewTreeObserver.OnGlobalFocusChangeListener {

        override fun onGlobalFocusChanged(oldFocus: View?, newFocus: View?) {
            onFocusChanged(rootView, newFocus)
        }
    }

    private class ActiveFocusData(
        val elementNodeInfo: List<ElementNodeInfo>,
        val legacyData: LegacyData,
        var targetElementPath: List<ElementNode>
    )
}
