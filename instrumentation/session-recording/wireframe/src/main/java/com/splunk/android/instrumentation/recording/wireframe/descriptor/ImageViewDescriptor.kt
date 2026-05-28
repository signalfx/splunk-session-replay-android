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

package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.widget.ImageView
import com.splunk.android.instrumentation.recording.wireframe.estimator.DrawableColorsEstimator
import com.splunk.android.instrumentation.recording.wireframe.extension.set
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import kotlin.math.max
import kotlin.math.min

internal open class ImageViewDescriptor : ViewDescriptor() {

    private val viewRect = Rect()
    private val skeletonRect = Rect()
    private val drawableRect = Rect()

    override val intendedClass: Class<*>? = ImageView::class.java

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is ImageView || view.drawable == null)
            return

        when (view.scaleType) {
            ImageView.ScaleType.MATRIX -> calcMatrixRect(view, skeletonRect, drawableRect)
            ImageView.ScaleType.FIT_XY -> calcFitXYRect(view, skeletonRect, drawableRect)
            ImageView.ScaleType.FIT_START -> calcFitStartRect(view, skeletonRect, drawableRect)
            ImageView.ScaleType.FIT_CENTER, null -> calcFitCenterRect(view, skeletonRect, drawableRect)
            ImageView.ScaleType.FIT_END -> calcFitEndRect(view, skeletonRect, drawableRect)
            ImageView.ScaleType.CENTER -> calcCenterRect(view, skeletonRect, drawableRect)
            ImageView.ScaleType.CENTER_CROP -> calcCenterCropRect(view, skeletonRect, drawableRect)
            ImageView.ScaleType.CENTER_INSIDE -> calcCenterInsideRect(view, skeletonRect, drawableRect)
        }

        viewRect.right = view.width
        viewRect.bottom = view.height

        if (skeletonRect.intersect(viewRect)) {
            val colors = DrawableColorsEstimator.estimate(view.drawable, drawableRect)

            if (colors.isClearlyVisible())
                result += Window.View.Skeleton.Color(
                    rect = Rect(skeletonRect),
                    clipRect = null,
                    type = Window.View.Skeleton.Color.Type.GENERAL,
                    colors = colors,
                    radii = null,
                    flags = null,
                    isOpaque = false
                )
        }
    }

    override fun getType(view: View): Window.View.Type {
        return if (view.isClickable)
            Window.View.Type.BUTTON
        else
            Window.View.Type.IMAGE
    }

    private fun calcMatrixRect(view: ImageView, skeletonRect: Rect, drawableRect: Rect) { // FIXME Wrong
        val matrix = view.imageMatrix
        val drawable = view.drawable

        if (matrix.isIdentity) {
            val left = view.paddingLeft
            val top = view.paddingTop
            val right = left + drawable.intrinsicWidth
            val bottom = top + drawable.intrinsicHeight

            skeletonRect.set(left, top, right, bottom)
            drawableRect.set(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        } else {
            val imgWidth = drawable.intrinsicWidth.toFloat()
            val imgHeight = drawable.intrinsicHeight.toFloat()

            val rectF = RectF(0f, 0f, imgWidth, imgHeight)
            matrix.mapRect(rectF)

            skeletonRect.set(rectF)
            skeletonRect.offset(view.paddingLeft, view.paddingTop)
            drawableRect.set(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        }
    }

    private fun calcFitXYRect(view: ImageView, skeletonRect: Rect, drawableRect: Rect) {
        val drawable = view.drawable

        val left = view.paddingLeft
        val top = view.paddingTop
        val right = view.width - view.paddingRight
        val bottom = view.height - view.paddingBottom

        skeletonRect.set(left, top, right, bottom)
        drawableRect.set(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }

    private fun calcFitStartRect(view: ImageView, skeletonRect: Rect, drawableRect: Rect) {
        val drawable = view.drawable

        val contentWidth = view.width - view.paddingLeft - view.paddingRight
        val contentHeight = view.height - view.paddingTop - view.paddingBottom
        val scaleX = contentWidth / drawable.intrinsicWidth.toFloat()
        val scaleY = contentHeight / drawable.intrinsicHeight.toFloat()
        val imgWidth = min(contentWidth, (drawable.intrinsicWidth * scaleY).toInt())
        val imgHeight = min(contentHeight, (drawable.intrinsicHeight * scaleX).toInt())

        val left = view.paddingLeft
        val top = view.paddingTop
        val right = view.paddingLeft + imgWidth
        val bottom = view.paddingTop + imgHeight

        skeletonRect.set(left, top, right, bottom)
        drawableRect.set(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }

    private fun calcFitCenterRect(view: ImageView, skeletonRect: Rect, drawableRect: Rect) {
        val drawable = view.drawable

        val intrinsicWidth = if (drawable.intrinsicWidth == -1) drawable.bounds.width() else drawable.intrinsicWidth
        val intrinsicHeight = if (drawable.intrinsicHeight == -1) drawable.bounds.height() else drawable.intrinsicHeight

        val contentWidth = view.width - view.paddingLeft - view.paddingRight
        val contentHeight = view.height - view.paddingTop - view.paddingBottom
        val scaleX = contentWidth / intrinsicWidth.toFloat()
        val scaleY = contentHeight / intrinsicHeight.toFloat()
        val imgWidth = min(contentWidth, (intrinsicWidth * scaleY).toInt())
        val imgHeight = min(contentHeight, (intrinsicHeight * scaleX).toInt())

        val left = view.paddingLeft + (contentWidth - imgWidth) / 2
        val top = view.paddingTop + (contentHeight - imgHeight) / 2
        val right = view.paddingLeft + (contentWidth + imgWidth) / 2
        val bottom = view.paddingTop + (contentHeight + imgHeight) / 2

        skeletonRect.set(left, top, right, bottom)
        drawableRect.set(0, 0, intrinsicWidth, intrinsicHeight)
    }

    private fun calcFitEndRect(view: ImageView, skeletonRect: Rect, drawableRect: Rect) {
        val drawable = view.drawable

        val intrinsicWidth = if (drawable.intrinsicWidth == -1) drawable.bounds.width() else drawable.intrinsicWidth
        val intrinsicHeight = if (drawable.intrinsicHeight == -1) drawable.bounds.height() else drawable.intrinsicHeight

        val contentWidth = view.width - view.paddingLeft - view.paddingRight
        val contentHeight = view.height - view.paddingTop - view.paddingBottom
        val scaleX = contentWidth / intrinsicWidth.toFloat()
        val scaleY = contentHeight / intrinsicHeight.toFloat()
        val imgWidth = min(contentWidth, (intrinsicWidth * scaleY).toInt())
        val imgHeight = min(contentHeight, (intrinsicHeight * scaleX).toInt())

        val left = view.paddingLeft + contentWidth - imgWidth
        val top = view.paddingTop + contentHeight - imgHeight
        val right = view.width - view.paddingRight
        val bottom = view.height - view.paddingBottom

        skeletonRect.set(left, top, right, bottom)
        drawableRect.set(0, 0, intrinsicWidth, intrinsicHeight)
    }

    private fun calcCenterRect(view: ImageView, skeletonRect: Rect, drawableRect: Rect) {
        val drawable = view.drawable

        val intrinsicWidth = if (drawable.intrinsicWidth == -1) drawable.bounds.width() else drawable.intrinsicWidth
        val intrinsicHeight = if (drawable.intrinsicHeight == -1) drawable.bounds.height() else drawable.intrinsicHeight

        val imgWidth = min(intrinsicWidth, view.width)
        val imgHeight = min(intrinsicHeight, view.height)

        val skeletonLeft = (view.width - imgWidth) / 2
        val skeletonTop = (view.height - imgHeight) / 2
        val skeletonRight = (view.width + imgWidth) / 2
        val skeletonBottom = (view.height + imgHeight) / 2

        skeletonRect.set(skeletonLeft, skeletonTop, skeletonRight, skeletonBottom)

        val drawableLeft = ((intrinsicWidth - view.width) / 2).coerceAtLeast(0)
        val drawableTop = ((intrinsicHeight - view.height) / 2).coerceAtLeast(0)
        val drawableRight = ((intrinsicWidth + view.width) / 2).coerceAtMost(intrinsicWidth)
        val drawableBottom = ((intrinsicHeight + view.height) / 2).coerceAtMost(intrinsicHeight)

        drawableRect.set(drawableLeft, drawableTop, drawableRight, drawableBottom)
    }

    private fun calcCenterCropRect(view: ImageView, skeletonRect: Rect, drawableRect: Rect) {
        val drawable = view.drawable

        val intrinsicWidth = if (drawable.intrinsicWidth == -1) drawable.bounds.width() else drawable.intrinsicWidth
        val intrinsicHeight = if (drawable.intrinsicHeight == -1) drawable.bounds.height() else drawable.intrinsicHeight

        val contentWidth = view.width - view.paddingLeft - view.paddingRight
        val contentHeight = view.height - view.paddingTop - view.paddingBottom
        val scaleX = contentWidth / intrinsicWidth.toFloat()
        val scaleY = contentHeight / intrinsicHeight.toFloat()
        val imgWidth = max(contentWidth, (intrinsicWidth * scaleY).toInt())
        val imgHeight = max(contentHeight, (intrinsicHeight * scaleX).toInt())
        val drawableScale = 1f / max(scaleX, scaleY)

        val skeletonLeft = view.paddingLeft + (contentWidth - imgWidth) / 2
        val skeletonTop = view.paddingTop + (contentHeight - imgHeight) / 2
        val skeletonRight = view.paddingLeft + (contentWidth + imgWidth) / 2
        val skeletonBottom = view.paddingTop + (contentHeight + imgHeight) / 2

        skeletonRect.set(skeletonLeft, skeletonTop, skeletonRight, skeletonBottom)

        val drawableLeft = -(skeletonLeft.coerceAtMost(0) * drawableScale + 0.5f).toInt()
        val drawableTop = -(skeletonTop.coerceAtMost(0) * drawableScale + 0.5f).toInt()
        val drawableRight = intrinsicWidth + ((skeletonRight - imgWidth).coerceAtMost(0) * drawableScale + 0.5f).toInt()
        val drawableBottom = intrinsicHeight + ((skeletonBottom - imgHeight).coerceAtMost(0) * drawableScale + 0.5f).toInt()

        drawableRect.set(drawableLeft, drawableTop, drawableRight, drawableBottom)
    }

    private fun calcCenterInsideRect(view: ImageView, skeletonRect: Rect, drawableRect: Rect) {
        val drawable = view.drawable

        val intrinsicWidth = if (drawable.intrinsicWidth == -1) drawable.bounds.width() else drawable.intrinsicWidth
        val intrinsicHeight = if (drawable.intrinsicHeight == -1) drawable.bounds.height() else drawable.intrinsicHeight

        val contentWidth = view.width - view.paddingLeft - view.paddingRight
        val contentHeight = view.height - view.paddingTop - view.paddingBottom
        val scaleX = contentWidth / intrinsicWidth.toFloat()
        val scaleY = contentHeight / intrinsicHeight.toFloat()
        val imgWidth = min(min(contentWidth, (intrinsicWidth * scaleY).toInt()), intrinsicWidth)
        val imgHeight = min(min(contentHeight, (intrinsicHeight * scaleX).toInt()), intrinsicHeight)

        val left = view.paddingLeft + (contentWidth - imgWidth) / 2
        val top = view.paddingTop + (contentHeight - imgHeight) / 2
        val right = view.paddingLeft + (contentWidth + imgWidth) / 2
        val bottom = view.paddingTop + (contentHeight + imgHeight) / 2

        skeletonRect.set(left, top, right, bottom)
        drawableRect.set(0, 0, intrinsicWidth, intrinsicHeight)
    }
}
