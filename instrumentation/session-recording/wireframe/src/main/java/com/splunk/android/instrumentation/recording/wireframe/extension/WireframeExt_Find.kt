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

package com.splunk.android.instrumentation.recording.wireframe.extension

import android.view.View
import com.splunk.rum.common.utils.extensions.identity
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

/**
 * Returns [Wireframe.Frame.Scene.Window.View] that represents instance of [view] or null if not found.
 */
fun Wireframe.Frame.Scene.Window.findViewByInstance(view: View): WireframeView? {
    return if (subviews != null)
        findViewByIdentity(view.identity, subviews)
    else
        null
}

/**
 * Returns [Wireframe.Frame.Scene.Window] that corresponds to root view.
 */
fun Wireframe.Frame.findWindow(view: View): Wireframe.Frame.Scene.Window? {
    val identity = view.identity

    for (i in scenes.indices) {
        val scene = scenes[i]

        for (j in scene.windows.indices) {
            val window = scene.windows[j]

            if (window.identity == identity)
                return window
        }
    }

    return null
}

private fun findViewByIdentity(identity: String, subviews: List<WireframeView>): WireframeView? {
    for (i in subviews.indices) {
        val view = subviews[i]

        if (view.identity == identity)
            return view

        return findViewByIdentity(identity, view.subviews ?: continue) ?: continue
    }

    return null
}

/**
 * Find all [WireframeView]s on path to [WireframeView] with [identity].
 */
fun Wireframe.Frame.findViewsOnPathToIdentity(identity: String, result: MutableList<WireframeView> = ArrayList()): List<WireframeView> {
    for (scene in scenes)
        for (window in scene.windows)
            for (view in window.subviews ?: continue)
                view.findViewsOnPathToIdentity(identity, result)

    return result
}

fun Wireframe.Frame.Scene.Window.View.findViewsOnPathToIdentity(identity: String, result: MutableList<WireframeView> = ArrayList()): List<WireframeView> {
    if (identity == this.identity)
        result += this
    else if (subviews != null)
        findViewsOnPathToIdentity(identity, subviews, result)

    return result
}

private fun findViewsOnPathToIdentity(identity: String, subviews: List<WireframeView>, result: MutableList<WireframeView>): Boolean {
    for (i in subviews.indices) {
        val view = subviews[i]

        if (view.identity == identity) {
            result += view
            return true
        }

        if (view.subviews != null) {
            result += view

            if (!findViewsOnPathToIdentity(identity, view.subviews, result))
                result.removeAt(result.lastIndex)
            else
                return true
        }
    }

    return false
}
