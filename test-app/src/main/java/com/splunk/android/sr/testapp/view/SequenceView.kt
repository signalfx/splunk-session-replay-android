package com.splunk.android.sr.testapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.splunk.android.common.utils.dpToPxF

class SequenceView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint()

    private var isMeasured = false

    private var wasDrawnInLayout = false
    private var wasDrawnBeforeAttach = false
    private var wasDrawnBeforeMeasure = false

    init {
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = dpToPxF(15f)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post { requestLayout() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        isMeasured = true
    }

    override fun onDraw(canvas: Canvas) {
        if (isLayoutRequested)
            wasDrawnInLayout = true

        if (!isAttachedToWindow)
            wasDrawnBeforeAttach = true

        if (!isMeasured)
            wasDrawnBeforeMeasure = true

        val x = width / 2f
        val y = (height - paint.ascent()) / 2f

        if (wasDrawnInLayout) {
            canvas.drawColor(Color.RED)
            canvas.drawText("Draw when layout is requested", x, y, paint)
            return
        }

        if (wasDrawnBeforeAttach) {
            canvas.drawColor(Color.RED)
            canvas.drawText("Draw before attach", x, y, paint)
            return
        }

        if (wasDrawnBeforeMeasure) {
            canvas.drawColor(Color.RED)
            canvas.drawText("Draw before measure", x, y, paint)
            return
        }

        canvas.drawColor(Color.GREEN)
        canvas.drawText("Correct", x, y, paint)
    }
}
