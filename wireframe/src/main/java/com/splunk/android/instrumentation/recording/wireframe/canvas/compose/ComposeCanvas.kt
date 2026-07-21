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

package com.splunk.android.instrumentation.recording.wireframe.canvas.compose

import android.graphics.Rect
import android.view.View
import com.splunk.rum.common.utils.MutableListObserver
import com.splunk.android.instrumentation.recording.wireframe.R
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.extension.WireframeView
import com.splunk.android.instrumentation.recording.wireframe.extension.findLastNotNullValue
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton
import java.util.LinkedList

/**
 * Correct draw order is [beginDraw] > [beginComposeElement] > [endComposeElement] > [addViewModifier] > [endDraw].
 * Functions [beginComposeElement], [endComposeElement] and [addViewModifier] are called from [SessionReplayDrawModifier].
 */
internal class ComposeCanvas : SkeletonCanvas() {

    private val layoutLevels = LinkedList<LayoutLevel>()

    private var isViewCreated = false

    val elements: MutableList<Element> = ArrayList()

    override val skeletons: MutableList<Skeleton> = MutableListObserver(ArrayList(), Observer())

    fun beginDraw() {
        isViewCreated = false

        skeletons.clear()
        elements.clear()
    }

    fun endDraw() {
        while (layoutLevels.isNotEmpty())
            endComposeElement()
    }

    fun beginComposeElement(modifier: SessionReplayDrawModifier, elementHash: Int) {
        val lastLayoutLevel = layoutLevels.lastOrNull()
        if (lastLayoutLevel != null && lastLayoutLevel.modifier == null)
            endComposeElement()

        val id = modifier.id ?: "_null"
        val isSensitive = modifier.isSensitive ?: layoutLevels.findLastNotNullValue { it.modifier?.isSensitive } ?: false
        val view = createView(id, isSensitive, elementHash.toString())

        layoutLevels += LayoutLevel(modifier, view)
        isTextSkeletonsAllowed = isSensitive == false
        isViewCreated = true
    }

    fun endComposeElement() {
        val layoutLevel = layoutLevels.removeLastOrNull() ?: throw IllegalStateException("Function beginElement was not called")
        val lastLayoutLevel = layoutLevels.lastOrNull()

        if (lastLayoutLevel != null) {
            val lastElementSubviews = lastLayoutLevel.view.subviews as MutableList<WireframeView>
            lastElementSubviews += layoutLevel.view
        } else
            elements += Element.Compose(layoutLevel.view)

        isTextSkeletonsAllowed = false
        isViewCreated = false
    }

    fun addViewModifier(view: View, modifier: SessionReplayDrawModifier) {
        view.setTag(R.id.sr_tag_is_sensitive, modifier.isSensitive)

        if (isViewCreated)
            endComposeElement()

        elements += Element.View(view, modifier)
    }

    private fun createView(id: String, isSensitive: Boolean, identity: String): WireframeView {
        return WireframeView(
            id = id,
            name = "JetpackComposeElement",
            rect = Rect(),
            type = null,
            typename = "JetpackComposeElement",
            hasFocus = false,
            offset = null,
            alpha = 1f,
            skeletons = ArrayList(),
            foregroundSkeletons = null,
            subviews = ArrayList(),
            identity = identity,
            isDrawDeterministic = true,
            isSensitive = isSensitive,
            subviewsLock = null
        )
    }

    private inner class Observer : MutableListObserver.Observer<Skeleton> {
        override fun onAdded(element: Skeleton) {
            var layoutLevel = layoutLevels.lastOrNull()

            if (layoutLevel == null || !isViewCreated) {
                val view = createView("", false, "")
                layoutLevel = LayoutLevel(null, view)

                layoutLevels += layoutLevel
                isViewCreated = true
            }

            layoutLevel.view.rect.union(element.rect)

            val skeletons = layoutLevel.view.skeletons as MutableList<Skeleton>
            skeletons += element
        }
    }

    private class LayoutLevel(
        val modifier: SessionReplayDrawModifier?,
        val view: WireframeView
    )

    sealed interface Element {
        data class Compose(val view: WireframeView) : Element
        data class View(val view: android.view.View, val modifier: SessionReplayDrawModifier) : Element
    }
}
