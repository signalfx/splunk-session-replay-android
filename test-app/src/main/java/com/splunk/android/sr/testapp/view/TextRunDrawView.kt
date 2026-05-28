package com.splunk.android.sr.testapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.text.MeasuredText
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.splunk.android.common.utils.dpToPxF

class TextRunDrawView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.textSize = dpToPxF(15f)
        paint.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        val contentWidth = width - paddingStart - paddingEnd
        val contentHeight = height - paddingTop - paddingBottom
        val lineCount = 9

        val xCenter = paddingStart + contentWidth / 2f
        val yCenter = paddingTop + contentHeight / 2f
        val lineHeight = paint.textSize
        val yStart = yCenter - (lineHeight * lineCount + LINE_MARGIN * (lineCount - 1)) / 2f - paint.ascent()

        var x = 0f
        var y = yStart

        val drawLine: (draw: () -> Unit) -> Unit = {
            it()
            y += lineHeight + LINE_MARGIN
        }

        val drawContent: () -> Unit = {
            drawLine {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    drawCharSequence(canvas, x, y)
                else
                    canvas.drawText("CharSequence run is not available on pre M", x, y, paint)
            }

            drawLine {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    drawCharArray(canvas, x, y)
                else
                    canvas.drawText("CharArray run is not available on pre M", x, y, paint)
            }

            drawLine {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    drawMeasuredText(canvas, x, y)
                else
                    canvas.drawText("MeasuredText run is not available on pre Q", x, y, paint)
            }
        }

        x = 0f
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.BLACK
        drawContent()

        x = xCenter
        paint.textAlign = Paint.Align.CENTER
        paint.isUnderlineText = true
        paint.color = 0xff8b0000.toInt()
        drawContent()
        paint.isUnderlineText = false

        x = width.toFloat()
        paint.textAlign = Paint.Align.RIGHT
        paint.isStrikeThruText = true
        paint.color = 0xff00008b.toInt()
        drawContent()
        paint.isStrikeThruText = false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun drawCharSequence(canvas: Canvas, x: Float, y: Float) {
        val text = "    Test message with CharSequence    "
        canvas.drawTextRun(text, 0, text.length, 0, text.length, x, y, false, paint)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun drawCharArray(canvas: Canvas, x: Float, y: Float) {
        val text = "    Test message with CharArray    ".toCharArray()
        canvas.drawTextRun(text, 0, text.size, 0, text.size, x, y, false, paint)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun drawMeasuredText(canvas: Canvas, x: Float, y: Float) {
        val text = "    Test message with MeasuredText    ".toCharArray()
        val measuredText = MeasuredText.Builder(text)
            .appendStyleRun(paint, text.size, false)
            .build()

        canvas.drawTextRun(measuredText, 0, text.size, 0, text.size, x, y, false, paint)
    }

    private companion object {
        val LINE_MARGIN: Float = dpToPxF(10f)
    }
}
