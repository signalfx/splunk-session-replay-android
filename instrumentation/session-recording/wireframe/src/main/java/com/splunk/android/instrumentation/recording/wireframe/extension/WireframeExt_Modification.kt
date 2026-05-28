package com.splunk.android.instrumentation.recording.wireframe.extension

import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

internal fun WireframeView.replaceView(id: String, replacement: WireframeView): Boolean {
    val subviews = subviews ?: return false

    for (i in subviews.indices) {
        val view = subviews[i]

        if (view.id == id) {
            subviews[i] = replacement
            return true
        }

        if (view.replaceView(id, replacement))
            return true
    }

    return false
}

/**
 * Remove all views which satisfy [predicate].
 */
internal fun Wireframe.Frame.Scene.Window.View.removeAll(predicate: (WireframeView) -> Boolean) {
    if (subviews != null)
        for (i in subviews.indices) {
            val view = subviews[i]

            if (predicate(view))
                subviews.removeAt(i)

            view.removeAll(predicate)
        }
}
