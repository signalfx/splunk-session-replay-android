package com.splunk.android.debugger.view.format

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.Keep
import com.splunk.android.debugger.view.format.PrettyCodeView.Formatter
import com.splunk.android.debugger.view.format.PrettyCodeView.LineInfo

@Keep
internal class PlainFormatter : Formatter { // Used in XML

    override fun drawLine(canvas: Canvas, paint: Paint, x: Float, y: Float, string: String, lineInfo: LineInfo) {
        paint.textAlign = Paint.Align.LEFT
        paint.typeface = TYPEFACE_NORMAL
        paint.color = Color.BLACK

        canvas.drawText(string, lineInfo.start, lineInfo.end, x, y, paint)
    }

    companion object {
        private val TYPEFACE_NORMAL = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }
}
