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
import com.splunk.rum.common.utils.Region
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

/**
 * Calculate visible rectangles for Views with [Wireframe.Frame.Scene.Window.View.isSensitive] is equal to true.
 * For example, when view's top right corner is covered by another view, three rectangles which form character L are returned.
 *
 * @return Map with [Wireframe.Frame.Scene.Window.View.identity] and list of [Rect].
 */
fun Wireframe.Frame.Scene.Window.calcSensitiveViewsVisibleRects(): Map<String, List<Rect>> {
    val result = HashMap<String, List<Rect>>()

    if (subviews == null)
        return result

    val region = Region()

    forEachView {
        if (it.isSensitive == true)
            result[it.identity] = calcSensitiveViewsVisibleRects(it, region).toList()
    }

    return result
}

private fun Wireframe.Frame.Scene.Window.calcSensitiveViewsVisibleRects(view: WireframeView, region: Region): List<Rect> {
    if (subviews == null)
        return emptyList()

    region.reset()
    subviews.calcSensitiveViewsVisibleRectsByIdentity(view.identity, region)
    region.clip(rect)

    return region.getResult()
}

private fun List<WireframeView>.calcSensitiveViewsVisibleRectsByIdentity(identity: String, region: Region): Boolean {
    var isFound = false

    for (i in indices) {
        val view = get(i)

        if (view.skeletons != null && region.hasArea())
            for (j in view.skeletons.indices) {
                val skeleton = view.skeletons[j]

                if (skeleton is Wireframe.Frame.Scene.Window.View.Skeleton.Color && skeleton.isOpaque)
                    region.clipOut(skeleton.rect)
            }

        if (view.foregroundSkeletons != null && region.hasArea())
            for (j in view.foregroundSkeletons.indices) {
                val skeleton = view.foregroundSkeletons[j]

                if (skeleton is Wireframe.Frame.Scene.Window.View.Skeleton.Color && skeleton.isOpaque)
                    region.clipOut(skeleton.rect)
            }

        if (view.identity == identity) {
            region.addArea(view.rect)
            isFound = true
        } else if (view.subviews != null)
            if (view.subviews.calcSensitiveViewsVisibleRectsByIdentity(identity, region)) {
                region.clip(view.rect)
                isFound = true
            }
    }

    return isFound
}
