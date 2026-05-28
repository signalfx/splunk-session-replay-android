package com.splunk.android.instrumentation.recording.screenshot.utils

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import com.splunk.android.common.utils.dpToPx

internal class SensitiveOverlayPaint : Paint(ANTI_ALIAS_FLAG) {

    init {
        val bitmap = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE, Bitmap.Config.ARGB_8888)

        strokeWidth = STROKE_WIDTH
        color = FOREGROUND_COLOR
        style = Style.STROKE

        val canvas = Canvas(bitmap)
        canvas.drawColor(BACKGROUND_COLOR)
        canvas.drawLine(-strokeWidth, strokeWidth, strokeWidth, -strokeWidth, this)
        canvas.drawLine(-strokeWidth, TILE_SIZE + strokeWidth, TILE_SIZE + strokeWidth, -strokeWidth, this)
        canvas.drawLine(TILE_SIZE - strokeWidth, TILE_SIZE + strokeWidth, TILE_SIZE + strokeWidth, TILE_SIZE - strokeWidth, this)

        shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        style = Style.FILL
    }

    private companion object {
        const val BACKGROUND_COLOR = 0xffe0e0e0.toInt()
        const val FOREGROUND_COLOR = 0xff787878.toInt()

        val TILE_SIZE = dpToPx(28f)
        val STROKE_WIDTH = TILE_SIZE * 0.35f
    }
}
