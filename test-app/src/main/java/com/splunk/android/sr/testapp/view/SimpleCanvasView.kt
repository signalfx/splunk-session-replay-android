package com.splunk.android.sr.testapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SimpleCanvasView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint()

    override fun onDraw(canvas: Canvas) {
        paint.color = Color.RED
        canvas.drawRect(0f, 0f, width.toFloat(), height * 0.3f, paint)

        paint.color = Color.BLUE
        canvas.drawRect(0f, height * 0.3f, width.toFloat(), height.toFloat(), paint)

        paint.color = Color.GREEN
        canvas.drawRect(0.3f * width, 0f, 0.5f * width, height.toFloat(), paint)
    }
}
