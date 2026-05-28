package com.splunk.android.debugger.view.seek

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

internal class ABMark(
    private val progress: Int,
    private val type: Type,
    color: Int
) : MarkableSeekBar.Mark {

    enum class Type { A, B }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    init {
        paint.color = color
    }

    override fun onDraw(view: MarkableSeekBar, canvas: Canvas) {
        val x = view.paddingStart + (view.width - view.paddingStart - view.paddingEnd) * (progress.toFloat() / view.max).coerceIn(0f, 1f)
        val contentHeight = (view.height - view.paddingTop - view.paddingBottom).toFloat()
        val shift = contentHeight * 0.1f

        when (type) {
            Type.A -> {
                path.moveTo(x, view.paddingTop.toFloat() + shift)
                path.lineTo(x, (view.height - view.paddingBottom).toFloat() - shift)
                path.lineTo(x + contentHeight / 2f, view.paddingTop + contentHeight / 2f)
            }
            Type.B -> {
                path.moveTo(x - contentHeight / 2f, view.paddingTop + contentHeight / 2f)
                path.lineTo(x, (view.height - view.paddingBottom).toFloat() - shift)
                path.lineTo(x, view.paddingTop.toFloat() + shift)
            }
        }

        path.close()
        canvas.drawPath(path, paint)
        path.reset()
    }
}
