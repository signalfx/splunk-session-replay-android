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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.drawerlayout

import android.graphics.Color
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.Colors
import com.splunk.rum.common.utils.extensions.get
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor
import com.splunk.android.instrumentation.recording.wireframe.extension.layoutDirectionCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.withAlpha
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

/* MARK
 *  - Compatible with androidx.drawerlayout.drawerlayout:1.0.0
 *  - Requires Proguard rules
 */
internal open class DrawerLayoutDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.drawerlayout.widget.DrawerLayout".toClass()

    override fun getForegroundSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getForegroundSkeletons(view, isSensitive, result)

        if (view !is DrawerLayout)
            return

        getDimSkeleton(view, result)
    }

    private fun getDimSkeleton(view: DrawerLayout, result: MutableList<Window.View.Skeleton>) {
        try {
            val opacity = view.get<Float>("mScrimOpacity") ?: error("Property 'mScrimOpacity' not found")

            if (opacity > 0f) {
                val menuView = findView(view, Gravity.HORIZONTAL_GRAVITY_MASK) ?: return
                val contentView = findView(view, Gravity.NO_GRAVITY) ?: return
                val layoutParams = menuView.layoutParams as DrawerLayout.LayoutParams

                var color = view.get<Int>("mScrimColor") ?: error("Property 'mScrimColor' not found")
                val alpha = Color.alpha(color) / 255f * opacity
                color = color.withAlpha(alpha)

                val isOpaque = alpha == 1f

                val rect = if (Gravity.getAbsoluteGravity(layoutParams.gravity, view.layoutDirectionCompat.constant) and Gravity.LEFT == Gravity.LEFT)
                    Rect(menuView.right, 0, contentView.right, view.height)
                else
                    Rect(0, 0, menuView.left, view.height)

                result += Window.View.Skeleton.Color(
                    rect = rect,
                    clipRect = null,
                    type = Window.View.Skeleton.Color.Type.GENERAL,
                    colors = Colors(color),
                    radii = null,
                    flags = null,
                    isOpaque = isOpaque
                )
            }
        } catch (e: Exception) {
            Logger.e1(TAG, "getDimSkeleton", e)
        }
    }

    private fun findView(view: DrawerLayout, gravity: Int): View? {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            val layoutParams = child.layoutParams as DrawerLayout.LayoutParams

            if (layoutParams.gravity == gravity || layoutParams.gravity and gravity > 0)
                return child
        }

        return null
    }

    private companion object {
        const val TAG = "DrawerLayoutDescriptor"
    }
}
