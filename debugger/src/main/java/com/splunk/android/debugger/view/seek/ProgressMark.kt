package com.splunk.android.debugger.view.seek

import android.graphics.Canvas
import android.graphics.Paint

internal class ProgressMark(
    private val progressA: Int,
    private val progressB: Int,
    color: Int
) : MarkableSeekBar.Mark {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.color = color
    }

    override fun onDraw(view: MarkableSeekBar, canvas: Canvas) {
        val xa = view.paddingStart + (view.width - view.paddingStart - view.paddingEnd) * (progressA.toFloat() / view.max).coerceIn(0f, 1f)
        val xb = view.paddingStart + (view.width - view.paddingStart - view.paddingEnd) * (progressB.toFloat() / view.max).coerceIn(0f, 1f)
        val y = view.paddingTop + (view.height - view.paddingTop - view.paddingBottom) / 2f
        val height = (view.height - view.paddingTop - view.paddingBottom) * 0.1f
        canvas.drawRect(xa, y - height / 2f, xb, y + height / 2f, paint)
    }
}
