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

package com.splunk.android.instrumentation.recording.capturer

import com.splunk.android.common.utils.extensions.forEachFast
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import java.util.LinkedList
import kotlin.math.max

class FrameHolder internal constructor() {

    private val frames = LinkedList<Wireframe.Frame>()
    private val screenshots = LinkedList<Screenshot>()

    /**
     * [Wireframe.Frame] limit in count of frames. Result of [getWireframeFrames], [getLastWireframeFrame] functions will be limited by the value.
     * Keep in mind, without real limitation an application can crash with OutOfMemoryException. One frame has around 15 kB in memory.
     * By default, history is limited to 200 frames (20 seconds with 10 fps).
     */
    var wireframeFramesCountLimit: Int = DEFAULT_WIREFRAME_LIMIT
        @Synchronized
        set(value) {
            field = value

            for (i in 0 until frames.size - value)
                frames.removeFirst()
        }

    /**
     * [Screenshot] limit in count of screenshots. Result of [getLastScreenshot] function will be limited by the value.
     * Keep in mind, without real limitation an application can crash with OutOfMemoryException. One screenshot of 700 * 400 pixels has 1.12 MB in memory.
     * By default, history is turned off.
     */
    var screenshotsCountLimit: Int = DEFAULT_SCREENSHOT_LIMIT
        set(value) {
            field = value

            for (i in 0 until screenshots.size - value) {
                val item = screenshots.removeFirst()
                item.bitmap.recycle()
            }
        }

    /**
     * Returns last [Wireframe.Frame] if available.
     *
     * @see wireframeFramesCountLimit
     */
    fun getLastWireframeFrame(): Wireframe.Frame? {
        return frames.lastOrNull()
    }

    /**
     * Returns list of available [Wireframe.Frame].
     *
     * @see wireframeFramesCountLimit
     */
    fun getWireframeFrames(): List<Wireframe.Frame> {
        return frames
    }

    /**
     * Return new list of available [Wireframe.Frame].
     *
     * @see wireframeFramesCountLimit
     */
    @Synchronized
    fun getWireframeFramesCopy(): List<Wireframe.Frame> {
        return frames.toList()
    }

    /**
     * Clears [Wireframe.Frame] buffer.
     */
    @Synchronized
    fun clearWireframeFrames() {
        frames.clear()
    }

    /**
     * Returns last [Screenshot] if available.
     *
     * @see screenshotsCountLimit
     */
    fun getLastScreenshot(): Screenshot? {
        return screenshots.lastOrNull()?.takeUnless { it.bitmap.isRecycled }
    }

    /**
     * Returns list of available [Screenshot].
     *
     * @see screenshotsCountLimit
     */
    fun getScreenshots(): List<Screenshot> {
        return screenshots
    }

    /**
     * Clears [Screenshot] buffer.
     */
    fun clearScreenshots() {
        val screenshotsCopy = screenshots.toList()
        screenshots.clear()
        screenshotsCopy.forEachFast { it.bitmap.recycle() }
    }

    @Synchronized
    internal fun storeWireframeFrame(frame: Wireframe.Frame, replaceLast: Boolean = false) {
        if (replaceLast && frames.isNotEmpty())
            frames[frames.lastIndex] = frame
        else {
            frames.limitCount(wireframeFramesCountLimit - 1)

            if (wireframeFramesCountLimit > 0)
                frames += frame
        }
    }

    internal fun storeScreenshot(screenshot: Screenshot) {
        screenshots.limitCount(screenshotsCountLimit - 1) { it.bitmap.recycle() }

        if (screenshotsCountLimit > 0)
            screenshots += screenshot
    }

    private fun <T> MutableList<T>.limitCount(count: Int, onRemoved: (T) -> Unit = {}) {
        for (i in 0 until max(size - count, 0)) {
            val removedItem = removeAt(0)
            onRemoved(removedItem)
        }
    }

    private companion object {
        const val DEFAULT_WIREFRAME_LIMIT = 10 * 20
        const val DEFAULT_SCREENSHOT_LIMIT = 0
    }
}
