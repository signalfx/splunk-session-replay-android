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

import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.splunk.rum.common.utils.Colors
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import kotlin.math.max
import kotlin.math.min

internal object SystemBarsExtractor {

    const val IDENTITY = "systemBars"

    private const val COLOR = 0x807C8697.toInt()

    fun extract(view: View, rect: Rect, isFloating: Boolean): Window? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return null

        val insets = view.rootWindowInsets ?: return null

        val (statusBars, navigationBars) = when {
            !isFloating ->
                getStatusBarsInsets(insets) to getNavigationBarsInsets(insets)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                getFloatingBarsInsetsFromMetrics(view, insets) ?: return null
            else ->
                getFloatingBarsInsetsFromDisplayFrame(view) ?: return null
        }

        val subviews = ArrayList<Window.View>()
        subviews.addBars("StatusBar", "com.android.systemui.StatusBar", rect, statusBars)
        subviews.addBars("NavigationBar", "com.android.systemui.NavigationBar", rect, navigationBars)

        if (subviews.isEmpty())
            return null

        return Window(
            id = IDENTITY,
            rect = Rect(rect),
            skeletons = null,
            subviews = subviews,
            identity = IDENTITY
        )
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getStatusBarsInsets(insets: WindowInsets): Rect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            insets.getInsets(WindowInsets.Type.statusBars())
                .let { Rect(it.left, it.top, it.right, it.bottom) }
        else
            Rect(0, min(insets.systemWindowInsetTop, insets.stableInsetTop), 0, 0)
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getNavigationBarsInsets(insets: WindowInsets): Rect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            insets.getInsets(WindowInsets.Type.navigationBars())
                .let { Rect(it.left, it.top, it.right, it.bottom) }
        else
            Rect(
                min(insets.systemWindowInsetLeft, insets.stableInsetLeft),
                0,
                min(insets.systemWindowInsetRight, insets.stableInsetRight),
                min(insets.systemWindowInsetBottom, insets.stableInsetBottom)
            )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getFloatingBarsInsetsFromMetrics(
        view: View,
        insets: WindowInsets
    ): Pair<Rect, Rect>? {
        val windowManager = view.context.getSystemService(WindowManager::class.java) ?: return null
        val screenInsets = windowManager.currentWindowMetrics.windowInsets

        val getInsets = { type: Int ->
            if (insets.isVisible(type))
                screenInsets.getInsetsIgnoringVisibility(type)
                    .let { Rect(it.left, it.top, it.right, it.bottom) }
            else
                Rect()
        }

        return getInsets(WindowInsets.Type.statusBars()) to getInsets(WindowInsets.Type.navigationBars())
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getFloatingBarsInsetsFromDisplayFrame(view: View): Pair<Rect, Rect>? {
        val display = view.display ?: return null

        val realSize = Point()
        display.getRealSize(realSize)

        val size = Point()
        display.getSize(size)

        val frame = Rect()
        view.getWindowVisibleDisplayFrame(frame)

        val horizontalLimit = max(realSize.x - size.x, 0)
        val verticalLimit = max(realSize.y - size.y, 0)

        val statusBars = Rect(0, max(frame.top, 0), 0, 0)
        val navigationBars = Rect(frame.left.coerceIn(0, horizontalLimit), 0, (realSize.x - frame.right).coerceIn(0, horizontalLimit), (realSize.y - frame.bottom).coerceIn(0, verticalLimit))

        return statusBars to navigationBars
    }

    private fun MutableList<Window.View>.addBars(name: String, typename: String, rect: Rect, insets: Rect) {
        if (insets.top > 0)
            this += createBar(name, typename, "top", Rect(rect.left, rect.top, rect.right, rect.top + insets.top))

        if (insets.bottom > 0)
            this += createBar(name, typename, "bottom", Rect(rect.left, rect.bottom - insets.bottom, rect.right, rect.bottom))

        if (insets.left > 0)
            this += createBar(name, typename, "left", Rect(rect.left, rect.top, rect.left + insets.left, rect.bottom))

        if (insets.right > 0)
            this += createBar(name, typename, "right", Rect(rect.right - insets.right, rect.top, rect.right, rect.bottom))
    }

    private fun createBar(name: String, typename: String, side: String, rect: Rect): Window.View {
        val id = "${IDENTITY}_${name}_$side"

        return Window.View(
            id = id,
            name = name,
            rect = rect,
            type = null,
            typename = typename,
            hasFocus = false,
            offset = null,
            alpha = 1f,
            skeletons = listOf(
                Window.View.Skeleton.Color(
                    rect = Rect(rect),
                    clipRect = null,
                    type = Window.View.Skeleton.Color.Type.GENERAL,
                    colors = Colors(COLOR),
                    radii = null,
                    flags = null,
                    isOpaque = false
                )
            ),
            foregroundSkeletons = null,
            subviews = null,
            identity = id,
            isDrawDeterministic = true,
            isSensitive = false,
            subviewsLock = null
        )
    }
}
