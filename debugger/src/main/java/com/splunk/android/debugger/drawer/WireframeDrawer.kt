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

package com.splunk.android.debugger.drawer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import androidx.annotation.IntDef
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.debugger.extension.drawRect
import com.splunk.android.debugger.extension.setColors
import com.splunk.android.debugger.extension.toDrawElements
import com.splunk.android.debugger.model.DrawElement
import com.splunk.android.debugger.util.BitmapCache
import com.splunk.android.instrumentation.recording.wireframe.extension.mulAlpha
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import kotlin.math.max

internal object WireframeDrawer { // FIXME Android 8 gradient issue on dashboard

    private val STROKE_WIDTH = max(dpToPxF(0.75f), 1f)
    private val CORNER_WIDTH = max(dpToPxF(1.25f), 2f)
    private val CORNER_LENGTH = dpToPxF(8f)

    private const val SHADOW_COLOR_DARK = 0x44000000
    private const val SHADOW_COLOR_LIGHT = 0x44ffffff
    private val SHADOW_RADIUS = dpToPxF(10f)
    private val SHADOW_DX = dpToPxF(1.5f)
    private val SHADOW_DY = dpToPxF(3f)

    const val FLAG_SKELETONS =
        0b00000001

    const val FLAG_VIEW_BORDERS =
        0b00000010

    const val FLAG_DEFAULT = FLAG_SKELETONS

    const val FLAG_ALL = FLAG_SKELETONS or FLAG_VIEW_BORDERS

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(FLAG_SKELETONS, FLAG_VIEW_BORDERS, FLAG_DEFAULT, FLAG_ALL)
    annotation class Flag

    private val bitmapCache = BitmapCache()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    fun draw(scene: Wireframe.Frame.Scene, @Flag flags: Int = FLAG_DEFAULT): Bitmap {
        val bitmap = Bitmap.createBitmap(scene.rect.width(), scene.rect.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        draw(canvas, scene.toDrawElements(), flags)
        return bitmap
    }

    fun draw(canvas: Canvas, drawElements: List<DrawElement>, @Flag flags: Int = FLAG_DEFAULT) { // FIXME BottomSheetDialog when view bounds only
        val drawSkeletons = flags and FLAG_SKELETONS == FLAG_SKELETONS
        val drawViewBounds = flags and FLAG_VIEW_BORDERS == FLAG_VIEW_BORDERS

        paint.style = Paint.Style.FILL

        for (element in drawElements)
            when {
                drawSkeletons && element is DrawElement.Color ->
                    drawColor(canvas, element)
                drawSkeletons && element is DrawElement.Text ->
                    drawText(canvas, element)
                drawViewBounds && element is DrawElement.ViewBorder ->
                    drawViewBound(canvas, element, drawSkeletons)
            }
    }

    private fun drawColor(canvas: Canvas, element: DrawElement.Color) {
        if (element.shadow != null) {
            val color = when (element.shadow) {
                DrawElement.Color.Shadow.DARK -> SHADOW_COLOR_DARK
                DrawElement.Color.Shadow.LIGHT -> SHADOW_COLOR_LIGHT
            }

            paint.setShadowLayer(SHADOW_RADIUS, SHADOW_DX, SHADOW_DY, color)
        } else
            paint.clearShadowLayer()

        canvas.save()

        if (element.clipRect != null)
            canvas.clipRect(element.clipRect)

        if (element.colors.isSingleColor()) {
            paint.color = element.colors[0, 0].mulAlpha(element.alpha)
            canvas.drawRect(element.rect, element.radii, paint)
        } else {
            if (element.shadow != null) {
                paint.color = Color.WHITE
                canvas.drawRect(element.rect, element.radii, paint)
            }

            val bitmap = bitmapCache.get(element.colors.width, element.colors.height)
            bitmap.setColors(element.colors)

            if (element.radii == null || element.radii[0] == 0f && element.radii[1] == 0f && element.radii[2] == 0f && element.radii[3] == 0f)
                canvas.drawBitmap(bitmap, null, element.rect, paint)
            else {
                path.reset()
                path.addRoundRect(element.rect, element.radii, Path.Direction.CW)

                canvas.clipPath(path)
                canvas.drawBitmap(bitmap, null, element.rect, paint)
            }
        }

        canvas.restore()
    }

    private fun drawText(canvas: Canvas, element: DrawElement.Text) {
        canvas.save()

        if (element.clipRect != null)
            canvas.clipRect(element.clipRect)

        paint.color = element.color
        paint.textSize = element.size
        paint.letterSpacing = element.letterSpacing
        paint.setTypeface(element.typeface)

        val y = element.rect.bottom - paint.descent()

        canvas.drawText(element.text, 0, element.text.length, element.rect.left, y, paint)

        canvas.restore()
    }

    private fun drawViewBound(canvas: Canvas, element: DrawElement.ViewBorder, isSkeletonsDraw: Boolean) {
        val rect = element.rect

        canvas.save()
        canvas.clipRect(element.clipRect)

        if (!isSkeletonsDraw)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // FIXME Why?

        paint.color = Color.RED

        canvas.drawRect(rect.left + CORNER_LENGTH, rect.top, rect.right - CORNER_LENGTH, rect.top + STROKE_WIDTH, paint) // top
        canvas.drawRect(rect.right - STROKE_WIDTH, rect.top + CORNER_LENGTH, rect.right, rect.bottom - CORNER_LENGTH, paint) // right
        canvas.drawRect(rect.left + CORNER_LENGTH, rect.bottom - STROKE_WIDTH, rect.right - CORNER_LENGTH, rect.bottom, paint) // bottom
        canvas.drawRect(rect.left, rect.top + CORNER_LENGTH, rect.left + STROKE_WIDTH, rect.bottom - CORNER_LENGTH, paint) // left

        paint.color = Color.BLUE

        // top left
        canvas.drawRect(rect.left, rect.top, rect.left + CORNER_WIDTH, rect.top + CORNER_LENGTH, paint)
        canvas.drawRect(rect.left + CORNER_WIDTH, rect.top, rect.left + CORNER_LENGTH, rect.top + CORNER_WIDTH, paint)

        // top right
        canvas.drawRect(rect.right - CORNER_LENGTH, rect.top, rect.right, rect.top + CORNER_WIDTH, paint)
        canvas.drawRect(rect.right - CORNER_WIDTH, rect.top + CORNER_WIDTH, rect.right, rect.top + CORNER_LENGTH, paint)

        // bottom right
        canvas.drawRect(rect.right - CORNER_WIDTH, rect.bottom - CORNER_LENGTH, rect.right, rect.bottom, paint)
        canvas.drawRect(rect.right - CORNER_LENGTH, rect.bottom - CORNER_WIDTH, rect.right - CORNER_WIDTH, rect.bottom, paint)

        // bottom left
        canvas.drawRect(rect.left, rect.bottom - CORNER_WIDTH, rect.left + CORNER_LENGTH, rect.bottom, paint)
        canvas.drawRect(rect.left, rect.bottom - CORNER_LENGTH, rect.left + CORNER_WIDTH, rect.bottom - CORNER_WIDTH, paint)

        canvas.restore()
    }
}
