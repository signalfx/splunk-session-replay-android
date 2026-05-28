package com.splunk.android.debugger.view.seek

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.widget.SeekBar

internal class MarkableSeekBar(context: Context, attrs: AttributeSet? = null) : SeekBar(context, attrs) { // FIXME Save state

    val marks = ArrayList<Mark>()

    init {
        if (isInEditMode) {
            marks += ABMark(12, ABMark.Type.A, Color.RED)
            marks += ABMark(38, ABMark.Type.B, Color.RED)
            marks += ProgressMark(12, 38, Color.RED)
        }
    }

    override fun onDraw(canvas: Canvas) {
        for (mark in marks)
            mark.onDraw(this, canvas)

        super.onDraw(canvas)
    }

    interface Mark {
        fun onDraw(view: MarkableSeekBar, canvas: Canvas)
    }
}
