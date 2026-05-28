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

package com.splunk.android.instrumentation.recording.interactions.extension

import com.splunk.android.instrumentation.recording.interactions.model.ElementNode
import com.splunk.android.instrumentation.recording.interactions.model.ElementNodeInfo
import com.splunk.android.instrumentation.recording.wireframe.extension.findViewsOnPathToIdentity
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import kotlin.math.max

private const val EXPECTED_MIN_PATH_SIZE = 10

private val targetViewPathCache = ArrayList<Wireframe.Frame.Scene.Window.View>(50)

/**
 * Resolves [ElementNode] path. This function is not thread safe!
 */
internal fun List<ElementNodeInfo>.resolve(frame: Wireframe.Frame, allowPartialPath: Boolean = false): List<ElementNode> {
    val rootElementNodeInfo = firstOrNull() ?: return emptyList()
    frame.findViewsOnPathToIdentity(rootElementNodeInfo.identity, targetViewPathCache)

    if (targetViewPathCache.isEmpty())
        return emptyList()

    val path = ArrayList<ElementNode>(max(size, EXPECTED_MIN_PATH_SIZE))
    path.append(targetViewPathCache, rootElementNodeInfo)
    targetViewPathCache.clear()

    for (i in 1 until size) {
        val lastView = path.last().view
        val node = get(i)

        lastView.findViewsOnPathToIdentity(node.identity, targetViewPathCache)

        if (targetViewPathCache.isEmpty())
            return if (allowPartialPath) path else emptyList()

        path.append(targetViewPathCache, node)
        targetViewPathCache.clear()
    }

    return path
}

private fun MutableList<ElementNode>.append(views: List<Wireframe.Frame.Scene.Window.View>, lastNodeInfo: ElementNodeInfo) {
    for (i in 0 until views.size - 1) {
        val view = views[i]

        add(
            ElementNode(
                view = view,
                positionInList = null,
                fragmentTag = null
            )
        )
    }

    add(
        ElementNode(
            view = views.last(),
            positionInList = lastNodeInfo.positionInList,
            fragmentTag = lastNodeInfo.fragmentTag
        )
    )
}

internal fun MutableList<ElementNodeInfo>.trimEnd(elementNode: List<ElementNode>) {
    val lastElement = elementNode.lastOrNull() ?: return
    val lastElementIdentity = lastElement.view.identity

    while (size > 0 && last().identity != lastElementIdentity)
        removeLast()
}
