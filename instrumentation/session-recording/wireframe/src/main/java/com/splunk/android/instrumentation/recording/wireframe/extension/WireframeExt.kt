package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Rect
import com.splunk.android.common.utils.extensions.forEachFast
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

typealias WireframeView = Wireframe.Frame.Scene.Window.View
typealias WireframeSkeleton = Wireframe.Frame.Scene.Window.View.Skeleton

val Wireframe.Frame.time: Long
    get() = scenes.first().time

val Wireframe.Frame.rect: Rect
    get() {
        val rect = Rect()

        scenes.forEachFast { scene ->
            rect.union(scene.rect)
        }

        return rect
    }

fun Wireframe.Frame.Scene.Window.forEachView(consumer: (view: WireframeView) -> Unit) {
    subviews?.forEachFast { subview ->
        subview.forEachView(consumer)
    }
}

private fun Wireframe.Frame.Scene.Window.View.forEachView(consumer: (view: WireframeView) -> Unit) {
    consumer(this)

    subviews?.forEachFast { subview ->
        subview.forEachView(consumer)
    }
}

internal fun WireframeView.getSkeletonCountRecursively(): Int {
    var skeletonCount = skeletons?.size ?: 0

    subviews?.forEachFast {
        skeletonCount += it.getSkeletonCountRecursively()
    }

    return skeletonCount
}

// Deterministic draw

val Wireframe.Frame.Scene.Window.isDrawDeterministic: Boolean
    get() {
        var localIsDrawDeterministic = true

        forEachView {
            if (!it.isDrawDeterministic) {
                localIsDrawDeterministic = false
                return@forEachView
            }
        }

        return localIsDrawDeterministic
    }
