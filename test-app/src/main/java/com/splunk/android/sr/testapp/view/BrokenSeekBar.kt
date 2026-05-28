package com.splunk.android.sr.testapp.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class BrokenSeekBar(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var fraction = 0f

    override fun onDraw(canvas: Canvas) {
        paint.color = Color.GRAY
        canvas.drawRect(paddingLeft.toFloat(), paddingTop.toFloat(), (width - paddingRight).toFloat(), (height - paddingBottom).toFloat(), paint)

        val handrailSize = (height - paddingTop - paddingBottom)
        val left = paddingLeft + fraction * (width - paddingLeft - paddingRight - handrailSize)

        paint.color = Color.RED
        canvas.drawRect(left, paddingTop.toFloat(), left + handrailSize, (height - paddingBottom).toFloat(), paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handrailSize = (height - paddingTop - paddingBottom)
        fraction = ((event.x - paddingLeft - handrailSize / 2f) / (width - paddingLeft - paddingRight - handrailSize)).coerceIn(0f, 1f)

        if (!IS_BROKEN)
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    parent.requestDisallowInterceptTouchEvent(false)
            }

        invalidate()
        return true
    }

    private companion object {
        const val IS_BROKEN = false
    }
}
