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

package com.splunk.android.instrumentation.recording.wireframe.estimator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import com.splunk.android.common.utils.Colors
import com.splunk.android.instrumentation.recording.wireframe.canvas.SwSafeCanvas
import com.splunk.android.instrumentation.recording.wireframe.extension.translate
import com.splunk.android.instrumentation.recording.wireframe.stats.StatsCollector

internal object DrawableColorsEstimator {

    private val canvas = SwSafeCanvas()

    private val bitmapColor = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private val bitmapGeneral = Bitmap.createBitmap(6, 6, Bitmap.Config.ARGB_8888)
    private val bitmapGeneralFallback = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

    fun estimate(drawable: Drawable, rect: Rect = drawable.bounds): Colors {
        StatsCollector.measureDrawableTime {
            val unwrappedDrawable = drawable.unwrap() ?: return Colors.TRANSPARENT

            when (unwrappedDrawable) {
                is ColorDrawable ->
                    return estimateColorDrawable(unwrappedDrawable)
                is BitmapDrawable ->
                    return estimateBitmapDrawable(unwrappedDrawable)
            }

            val allowMultipleColors = isAppropriateForMultipleColors(unwrappedDrawable)

            return estimate(unwrappedDrawable, rect, bitmapGeneral, allowMultipleColors).takeIf { it.isVisible() }
                ?: estimate(unwrappedDrawable, rect, bitmapGeneralFallback, allowMultipleColors)
        }

        return Colors.TRANSPARENT
    }

    private fun isAppropriateForMultipleColors(drawable: Drawable): Boolean {
        return when {
            drawable is GradientDrawable ->
                true
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && drawable is LayerDrawable -> {
                for (i in 0 until drawable.numberOfLayers) {
                    if (drawable.getId(i) == android.R.id.mask)
                        continue

                    if (drawable.getDrawable(i) !is GradientDrawable)
                        return false
                }

                return true
            }
            else ->
                false
        }
    }

    private fun estimateColorDrawable(drawable: ColorDrawable): Colors {
        bitmapColor.setPixel(0, 0, Color.TRANSPARENT)
        canvas.setBitmap(bitmapColor)

        val bounds = drawable.bounds
        val saveCount = canvas.save()
        canvas.translate(-bounds.left, -bounds.top)
        drawable.draw(canvas)
        canvas.restoreToCount(saveCount)

        return Colors(bitmapColor.getPixel(0, 0))
    }

    private fun estimateBitmapDrawable(drawable: BitmapDrawable): Colors {
        val bitmap = drawable.bitmap ?: return Colors.TRANSPARENT

        return BitmapColorsEstimator.estimate(
            bitmap = bitmap,
            approximation = true,
            paint = drawable.paint,
            allowMultipleColors = true
        )
    }

    private fun estimate(drawable: Drawable, rect: Rect, bitmap: Bitmap, allowMultipleColors: Boolean): Colors {
        val maxSize = bitmap.width.toFloat()
        val width = rect.width()
        val height = rect.height()

        val finalWidth: Float
        val finalHeight: Float

        if (width > height) {
            finalWidth = maxSize
            finalHeight = (maxSize / width * height).coerceAtLeast(1f)
        } else {
            finalWidth = (maxSize / height * width).coerceAtLeast(1f)
            finalHeight = maxSize
        }

        val scaleW = finalWidth / width
        val scaleH = finalHeight / height

        bitmap.eraseColor(Color.TRANSPARENT)

        useCanvas {
            it.setBitmap(bitmap)
            it.scale(scaleW, scaleH)
            it.translate(-rect.left, -rect.top)
            drawable.draw(it)
        }

        return BitmapColorsEstimator.estimate(
            bitmap = bitmap,
            bottom = finalHeight.toInt(),
            right = finalWidth.toInt(),
            allowMultipleColors = allowMultipleColors
        )
    }

    private inline fun useCanvas(crossinline block: (Canvas) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val saveCount = canvas.save()
            block(canvas)
            canvas.restoreToCount(saveCount)
        } else
            block(Canvas())
    }

    internal fun Drawable.unwrap(): Drawable? {
        return when {
            this is DrawableContainer ->
                @Suppress("UNNECESSARY_SAFE_CALL")
                current?.unwrap() // Can be null in implementation
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this is DrawableWrapper ->
                drawable?.unwrap()
            this is LayerDrawable -> {
                for (i in 0 until numberOfLayers)
                    if (getId(i) != android.R.id.mask)
                        return getDrawable(i)?.unwrap() ?: continue

                this
            }
            else ->
                this
        }
    }
}
