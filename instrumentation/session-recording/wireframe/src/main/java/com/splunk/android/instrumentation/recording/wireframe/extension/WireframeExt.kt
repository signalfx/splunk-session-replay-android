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

import android.graphics.Rect
import com.splunk.rum.common.utils.extensions.forEachFast
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
