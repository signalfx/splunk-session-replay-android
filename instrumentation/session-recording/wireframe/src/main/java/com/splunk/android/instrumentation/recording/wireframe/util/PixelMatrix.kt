package com.splunk.android.instrumentation.recording.wireframe.util

import android.graphics.Color

internal class PixelMatrix(
    val width: Int,
    val height: Int
) {

    private val pixels = Array(width * height) { Pixel() }

    operator fun get(x: Int, y: Int): Pixel {
        return pixels[x + y * width]
    }

    operator fun get(i: Int): Pixel {
        return pixels[i]
    }

    fun reset() {
        for (pixel in pixels)
            pixel.reset()
    }

    class Pixel {

        private var count = 0

        private var a = 0
        private var r = 0
        private var g = 0
        private var b = 0

        fun add(a: Int, r: Int, g: Int, b: Int) {
            this.a += a
            this.r += r
            this.g += g
            this.b += b

            count++
        }

        fun averageColor(): Int {
            return if (count == 0)
                Color.TRANSPARENT
            else
                Color.argb(a / count, r / count, g / count, b / count)
        }

        fun reset() {
            a = 0
            r = 0
            g = 0
            b = 0
            count = 0
        }
    }
}
