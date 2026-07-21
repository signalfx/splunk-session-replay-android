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

package com.splunk.android.instrumentation.recording.wireframe

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import com.splunk.rum.common.utils.extensions.forEachFast
import com.splunk.rum.common.utils.extensions.isUiContextCompat
import com.splunk.rum.common.utils.extensions.minusAssign
import com.splunk.rum.common.utils.extensions.sortedItemsByDecorViews
import com.splunk.android.instrumentation.recording.wireframe.extension.forEachView
import com.splunk.android.instrumentation.recording.wireframe.extension.orientation
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats

// FIXME Wireframe during the changing size in split screen mode is broken

class WireframeConstructor(private val listener: Listener) {

    private val windowsCache = LinkedHashMap<View, Wireframe.Frame.Scene.Window>()
    private val correctedWindows = HashSet<Wireframe.Frame.Scene.Window>()

    private var rootWindow: Wireframe.Frame.Scene.Window? = null
    private val rootWindowOffset = Point()

    private var orientation = Wireframe.Frame.Scene.Orientation.PORTRAIT
    private var isContentChanged = false
    private var timestamp = 0L

    fun openNewFrame(context: Context) {
        timestamp = System.currentTimeMillis() // TODO Use SystemClock.uptimeMillis() to correctly handle time changes
        orientation = if (context.isUiContextCompat) context.orientation else orientation
        isContentChanged = false
    }

    fun updateView(view: View): Wireframe.Frame.Scene.Window? {
        val newWindow = WireframeExtractor.extract(view)
        val currentWindow = windowsCache[view]

        if (currentWindow != newWindow) {
            if (newWindow != null)
                windowsCache[view] = newWindow
            else
                windowsCache -= view

            if (currentWindow === rootWindow) {
                rootWindowOffset.set(0, 0)
                rootWindow = null
            }

            correctedWindows -= currentWindow
            isContentChanged = true
        }

        return newWindow
    }

    fun removeView(view: View) {
        val window = windowsCache.remove(view)

        if (window === rootWindow) {
            rootWindowOffset.set(0, 0)
            rootWindow = null
        }

        correctedWindows -= window
        isContentChanged = true
    }

    fun closeFrame() {
        val windows = windowsCache.toList().sortedItemsByDecorViews()

        correctPositions(windows)

        val stats = WireframeExtractor.popWireframeStats()
        val frame = Wireframe.Frame(
            scenes = listOf(
                Wireframe.Frame.Scene(
                    id = "1",
                    time = timestamp,
                    rect = windows.firstOrNull()?.rect?.copy() ?: Rect(),
                    orientation = orientation,
                    type = Wireframe.Frame.Scene.Type.DEVICE,
                    windows = windows
                )
            )
        )

        listener.onNewFrame(frame, stats, isContentChanged)
    }

    fun clear() {
        isContentChanged = true
        windowsCache.clear()
    }

    private fun correctPositions(windows: List<Wireframe.Frame.Scene.Window>) {
        val rootWindow = windows.firstOrNull() ?: return
        val rootWindowRectOrigin: Rect

        if (rootWindow !in correctedWindows) {
            rootWindowOffset.set(-rootWindow.rect.left, -rootWindow.rect.top)
            this.rootWindow = rootWindow

            rootWindowRectOrigin = rootWindow.rect
        } else if (rootWindowOffset.x != 0 || rootWindowOffset.y != 0) {
            rootWindowRectOrigin = Rect(rootWindow.rect)
            rootWindowRectOrigin.offset(-rootWindowOffset.x, -rootWindowOffset.y)
        } else
            rootWindowRectOrigin = rootWindow.rect

        for (window in windows) {
            window.skeletons?.forEachFast {
                it.rect.fitIfNeeded(rootWindowRectOrigin)
            }

            window.rect.fitIfNeeded(rootWindowRectOrigin)
        }

        if (rootWindowOffset.x != 0 || rootWindowOffset.y != 0) {
            val offsetSkeleton: (Wireframe.Frame.Scene.Window.View.Skeleton) -> Unit = {
                it.rect.offset(rootWindowOffset)
                it.clipRect?.offset(rootWindowOffset)
            }

            windows.forEachFast { window ->
                if (window in correctedWindows)
                    return@forEachFast

                window.rect.offset(rootWindowOffset)
                window.skeletons?.forEachFast { offsetSkeleton(it) }

                window.forEachView { view ->
                    view.rect.offset(rootWindowOffset)

                    view.skeletons?.forEachFast { offsetSkeleton(it) }
                    view.foregroundSkeletons?.forEachFast { offsetSkeleton(it) }
                }

                correctedWindows += window
            }
        }
    }

    private fun Rect.fitIfNeeded(rect: Rect) {
        if (left == Int.MIN_VALUE)
            left = rect.left

        if (top == Int.MIN_VALUE)
            top = rect.top

        if (right == Int.MAX_VALUE)
            right = rect.right

        if (bottom == Int.MAX_VALUE)
            bottom = rect.bottom
    }

    private fun Rect.offset(point: Point) {
        offset(point.x, point.y)
    }

    private fun Rect.copy(): Rect {
        return Rect(this)
    }

    interface Listener {
        fun onNewFrame(frame: Wireframe.Frame, stats: WireframeStats, isChanged: Boolean)
    }
}
