package com.splunk.android.instrumentation.recording.wireframe.estimator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import com.splunk.android.common.utils.Colors
import com.splunk.android.common.utils.extensions.copyOrNull
import com.splunk.android.instrumentation.recording.wireframe.extension.hasEffect
import com.splunk.android.instrumentation.recording.wireframe.extension.withAlpha
import com.splunk.android.instrumentation.recording.wireframe.extension.withColor
import com.splunk.android.instrumentation.recording.wireframe.util.PixelMatrixCache
import java.util.WeakHashMap
import kotlin.math.max

internal object BitmapColorsEstimator {

    private const val DEFAULT_CACHE_CAPACITY = 512
    private const val APPROXIMATION_SAMPLE_COUNT = 6

    private val tempBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private val tempCanvas = Canvas(tempBitmap)

    private val pixelMatrixCache = PixelMatrixCache()

    private val cache = WeakHashMap<Bitmap, Entry>(DEFAULT_CACHE_CAPACITY)

    fun estimate(bitmap: Bitmap, left: Int = 0, top: Int = 0, right: Int = bitmap.width, bottom: Int = bitmap.height, approximation: Boolean = false, paint: Paint? = null, allowMultipleColors: Boolean = false): Colors {
        if (bitmap.isRecycled)
            return Colors.TRANSPARENT

        val cacheEntry = cache[bitmap]
        val paintHash = paint?.internalHashCode() ?: 0

        if (cacheEntry?.let { it.left == left && it.top == top && it.right == right && it.bottom == bottom && it.approximation == approximation && it.allowMultipleColors == allowMultipleColors && it.generationId == bitmap.generationId && it.paintHash == paintHash } == true)
            return cacheEntry.colors

        val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE) {
            val swBitmap = bitmap.copyOrNull(Bitmap.Config.ARGB_8888, false)

            if (swBitmap == null) {
                if (cacheEntry != null)
                    cache.remove(bitmap)

                return Colors.TRANSPARENT
            }

            estimateSoftwareBitmap(swBitmap, left, top, right, bottom, approximation, paint, allowMultipleColors)
        } else
            estimateSoftwareBitmap(bitmap, left, top, right, bottom, approximation, paint, allowMultipleColors)

        cache[bitmap] = Entry(left, top, right, bottom, approximation, paintHash, allowMultipleColors, bitmap.generationId, colors)
        return colors
    }

    private fun estimateSoftwareBitmap(bitmap: Bitmap, left: Int, top: Int, right: Int, bottom: Int, approximation: Boolean, paint: Paint?, allowMultipleColors: Boolean = false): Colors {
        val width = right - left
        val height = bottom - top

        var totalCount = 0
        var usableCount = 0

        val beginX: Int
        val beginY: Int
        val step: Int

        if (approximation) {
            val base = if (width > height) width else height
            step = max(base / (APPROXIMATION_SAMPLE_COUNT + 1), 1)

            beginX = left + width / step / 2
            beginY = top + height / step / 2
        } else {
            beginX = left
            beginY = top
            step = 1
        }

        val pw = if (allowMultipleColors && width > 1) 2 else 1
        val ph = if (allowMultipleColors && height > 1) 2 else 1

        val pixelMatrix = pixelMatrixCache.get(pw, ph)

        for (y in beginY until bottom step step)
            for (x in beginX until right step step) {
                val color = bitmap.getPixel(x, y)
                val alpha = Color.alpha(color)

                totalCount++

                if (alpha < 0x10)
                    continue

                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)

                val px = (pixelMatrix.width * x / right.toFloat()).toInt()
                val py = (pixelMatrix.height * y / bottom.toFloat()).toInt()

                pixelMatrix[px, py].add(alpha, r, g, b)
                usableCount++
            }

        return if (usableCount > 0) {
            val alphaFactor = 1 - (1 - usableCount / totalCount.toFloat()).let { it * it }
            val colors = Colors(pixelMatrix.width, pixelMatrix.height)

            for (i in 0 until colors.colors.size) {
                val color = pixelMatrix[i].averageColor()
                val estimatedAlpha = (255 - (255 - Color.alpha(color)) * alphaFactor).toInt()

                colors[i] = color.withAlpha(estimatedAlpha)
            }

            if (paint?.hasEffect() == true)
                for (i in 0 until colors.colors.size) {
                    tempBitmap.setPixel(0, 0, Color.TRANSPARENT)
                    paint.withColor(colors[i]) { tempCanvas.drawPaint(it) }
                    colors[i] = tempBitmap.getPixel(0, 0)
                }

            colors
        } else
            Colors.TRANSPARENT
    }

    private fun Paint.internalHashCode(): Int {
        var hashCode = color
        hashCode = 31 * hashCode + (xfermode?.hashCode() ?: 0)
        hashCode = 31 * hashCode + (colorFilter?.hashCode() ?: 0)
        hashCode = 31 * hashCode + (maskFilter?.hashCode() ?: 0)
        hashCode = 31 * hashCode + (pathEffect?.hashCode() ?: 0)
        hashCode = 31 * hashCode + (shader?.hashCode() ?: 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            hashCode = 31 * hashCode + (blendMode?.hashCode() ?: 0)

        return hashCode
    }

    private class Entry(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
        val approximation: Boolean,
        val paintHash: Int,
        val allowMultipleColors: Boolean,
        val generationId: Int,
        val colors: Colors
    )
}
