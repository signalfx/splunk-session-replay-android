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

package com.splunk.android.debugger.extension

import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import com.splunk.rum.common.utils.extensions.toRectF
import com.splunk.android.debugger.model.DrawElement
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal fun Wireframe.Frame.Scene.toDrawElements(): List<DrawElement> {
    val elements = ArrayList<DrawElement>(200)

    for (window in windows) {
        window.skeletons?.let { mapSkeletons(1f, it, elements) }
        window.subviews?.let { mapViews(it, window.rect, 1f, elements) }
    }

    return elements
}

private fun mapViews(views: List<Window.View>, clipRect: Rect, parentAlpha: Float, result: MutableList<DrawElement>) {
    for (view in views) {
        val alpha = parentAlpha * view.alpha
        if (alpha == 0f)
            continue

        val viewRect = Rect(view.rect)
        val isViewIntersect = viewRect.intersect(clipRect)

        view.skeletons?.let { mapSkeletons(alpha, it, result) }

        if (isViewIntersect)
            result += DrawElement.ViewBorder(view.rect.toRectF(), viewRect)

        view.subviews?.let { mapViews(it, viewRect, alpha, result) }
        view.foregroundSkeletons?.let { mapSkeletons(alpha, it, result) }
    }
}

private fun mapSkeletons(alpha: Float, skeletons: List<Window.View.Skeleton>, result: MutableList<DrawElement>) {
    for (skeleton in skeletons)
        result += when (skeleton) {
            is Window.View.Skeleton.Color -> {
                val shadow = when (skeleton.flags?.shadow) {
                    Window.View.Skeleton.Color.Flags.Shadow.LIGHT -> DrawElement.Color.Shadow.LIGHT
                    Window.View.Skeleton.Color.Flags.Shadow.DARK -> DrawElement.Color.Shadow.DARK
                    null -> null
                }

                val radii = when (skeleton.type) {
                    Window.View.Skeleton.Color.Type.GENERAL ->
                        skeleton.radii?.toFloatArray()
                    Window.View.Skeleton.Color.Type.TEXT -> {
                        val radius = skeleton.rect.height() / 2f
                        FloatArray(8) { radius }
                    }
                }

                DrawElement.Color(
                    rect = skeleton.rect.toRectF(),
                    clipRect = skeleton.clipRect,
                    radii = radii,
                    colors = skeleton.colors,
                    alpha = (alpha * 255).toInt(),
                    shadow = shadow
                )
            }
            is Window.View.Skeleton.Text -> {
                DrawElement.Text(
                    rect = skeleton.rect.toRectF(),
                    clipRect = skeleton.clipRect,
                    text = skeleton.text,
                    color = skeleton.color,
                    size = skeleton.size,
                    typeface = skeleton.font.toTypeface(),
                    letterSpacing = skeleton.letterSpacing
                )
            }
        }
}

private fun Window.View.Skeleton.Text.Font.toTypeface(): Typeface {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        Typeface.create(Typeface.create(familyName, Typeface.NORMAL), weight, isItalic)
    else {
        val isBold = weight >= 700

        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isItalic -> Typeface.ITALIC
            isBold -> Typeface.BOLD
            else -> Typeface.NORMAL
        }

        Typeface.create(familyName, style)
    }
}
