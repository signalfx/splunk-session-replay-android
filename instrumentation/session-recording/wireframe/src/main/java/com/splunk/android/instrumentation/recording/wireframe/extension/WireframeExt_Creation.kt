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
import com.splunk.rum.common.utils.Colors
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import java.util.LinkedList

/**
 * Creates [Wireframe] from frames.
 *
 * @param frames sorted list (by [Wireframe.Frame.Scene.time]) of frames.
 * @param startTime UNIX time in milliseconds of the first frame in [Wireframe].
 * @param endTime UNIX time in milliseconds of the last frame in [Wireframe].
 * @param exactEnds whether the first and last frame should be in [startTime] and [endTime]. When true, first and last frame can be copied.
 *
 * @return Wireframe structure with relative [Wireframe.frames] to [startTime].
 */
fun Wireframe.Companion.create(frames: List<Wireframe.Frame>, startTime: Long = 0L, endTime: Long = Long.MAX_VALUE, exactEnds: Boolean = true): Wireframe {
    if (startTime > endTime)
        throw IllegalArgumentException("Argument startTime '$startTime' can not be higher than endTime '$endTime'")

    val relativeFrames = LinkedList<Wireframe.Frame>()

    // Filter out frames which are in the [startTime] and [endTime] boundaries.
    for (i in frames.indices) {
        val frame = frames[i]
        val scene = frame.scenes.first()

        if (scene.time < startTime)
            continue

        if (scene.time > endTime)
            break

        val relativeScene = scene.copy(time = scene.time - startTime)
        relativeFrames += Wireframe.Frame(listOf(relativeScene))
    }

    // If the list is empty try to add at least last frame prior to start time if there is any.
    if (relativeFrames.isEmpty()) {
        val firstSceneValidPriorToStartTime = frames.lastOrNull { it.scenes.first().time <= startTime }?.scenes?.first()

        if (firstSceneValidPriorToStartTime != null)
            relativeFrames.add(0, Wireframe.Frame(listOf(firstSceneValidPriorToStartTime.copy(time = 0))))
    }

    if (exactEnds && relativeFrames.isNotEmpty()) {
        val firstScene = relativeFrames.first().scenes.first()
        val lastScene = relativeFrames.last().scenes.first()

        if (firstScene.time != 0L) {
            val firstSceneValidPriorToStartTime = frames.lastOrNull { it.scenes.first().time <= startTime }?.scenes?.first()
            val firstValidScene = (firstSceneValidPriorToStartTime ?: firstScene).copy(time = 0)
            relativeFrames.add(0, Wireframe.Frame(listOf(firstValidScene)))
        }

        if (lastScene.time != endTime - startTime) {
            val lastSceneAtTime = lastScene.copy(time = endTime - startTime)
            relativeFrames += Wireframe.Frame(listOf(lastSceneAtTime))
        }
    }

    return Wireframe(relativeFrames)
}

/**
 * Creates [Wireframe.Frame] for NO_RECORDING mode with one skeleton of #7C8697 color.
 */
fun Wireframe.Frame.Companion.createEmpty(rect: Rect, time: Long): Wireframe.Frame {
    return Wireframe.Frame(
        scenes = listOf(
            Wireframe.Frame.Scene(
                id = "",
                time = time,
                rect = rect,
                orientation = null,
                type = Wireframe.Frame.Scene.Type.DEVICE,
                windows = listOf(
                    Wireframe.Frame.Scene.Window(
                        id = "",
                        rect = rect,
                        skeletons = listOf(
                            Wireframe.Frame.Scene.Window.View.Skeleton.Color(
                                type = Wireframe.Frame.Scene.Window.View.Skeleton.Color.Type.GENERAL,
                                colors = Colors(0xff7C8697.toInt()),
                                radii = null,
                                rect = rect,
                                clipRect = null,
                                flags = null,
                                isOpaque = true
                            )
                        ),
                        subviews = null,
                        identity = ""
                    )
                )
            )
        )
    )
}
