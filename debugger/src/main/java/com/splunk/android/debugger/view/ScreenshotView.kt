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

package com.splunk.android.debugger.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import com.splunk.android.debugger.drawer.KeyboardDrawer
import com.splunk.android.debugger.extension.aspectRatio
import com.splunk.android.debugger.util.TransparentLayerPaint
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot

internal class ScreenshotView(context: Context, attrs: AttributeSet? = null) : WrapContentView(context, attrs) {

    private val transparentLayerPaint = TransparentLayerPaint()

    private val dstRect = Rect()

    var screenshot: Screenshot? = null
        set(value) {
            field = value

            requestLayout()
            invalidate()
        }

    var interactions: List<Interaction>? = null
        set(value) {
            field = value
            invalidate()
        }

    var isKeyboardOverlayEnabled = true
        set(value) {
            field = value
            invalidate()
        }

    override fun getContentAspectRatio(): Float {
        return screenshot?.bitmap?.aspectRatio ?: 0f
    }

    override fun onDraw(canvas: Canvas) {
        val bitmap = screenshot?.bitmap ?: return

        dstRect.right = width
        dstRect.bottom = height

        canvas.drawPaint(transparentLayerPaint)
        canvas.drawBitmap(bitmap, null, dstRect, null)

        if (isKeyboardOverlayEnabled) {
            val interactions = interactions ?: return
            KeyboardDrawer.draw(canvas, bitmap.width, bitmap.height, width, height, interactions)
        }
    }
}
