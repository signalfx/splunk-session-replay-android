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

package com.splunk.android.debugger.view.format

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import com.splunk.android.common.utils.dpToPx
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.debugger.R
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

internal class PrettyCodeView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val velocityTracker = VelocityTracker.obtain()
    private val scroller = OverScroller(context)

    private val lines = ArrayList<LineInfo>()
    private var longestLineIndex = -1

    private val maxVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity.toFloat()
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    var formatter: Formatter = PlainFormatter()
        set(value) {
            field = value
            invalidate()
        }

    var string: String? = null
        set(value) {
            field = value
            lines.clear()
            // TODO Update scrollY
            invalidate()
        }

    var isLineNumberVisible = true
        set(value) {
            field = value
            invalidate()
        }

    init {
        isFocusable = true
        isClickable = true
        isScrollContainer = true

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.PrettyCodeView)
            val formatterName = a.getString(R.styleable.PrettyCodeView_pretty_formatter)
            paint.textSize = a.getDimension(R.styleable.PrettyCodeView_pretty_fontSize, dpToPxF(15f))
            string = a.getString(R.styleable.PrettyCodeView_pretty_string)
            isLineNumberVisible = a.getBoolean(R.styleable.PrettyCodeView_pretty_lineNumber, isLineNumberVisible)
            a.recycle()

            if (formatterName != null) {
                val clazz = Class.forName(formatterName)

                if (Formatter::class.java !in clazz.interfaces)
                    throw IllegalArgumentException("Class from 'format' property must implements PrettyCodeView.Formatter")

                val constructor = clazz.getDeclaredConstructor()
                formatter = constructor.newInstance() as Formatter
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scroller.isFinished)
                    scroller.abortAnimation()

                velocityTracker.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker.addMovement(event)

                val deltaX = lastTouchX - event.x
                val deltaY = lastTouchY - event.y

                scrollBy(deltaX.toInt(), deltaY.toInt())
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker.computeCurrentVelocity(1000, maxVelocity)
                val xVelocity = -velocityTracker.xVelocity.toInt()
                val yVelocity = -velocityTracker.yVelocity.toInt()
                val maxX = getMaxScrollX()
                val maxY = getMaxScrollY()

                scroller.fling(scrollX, scrollY, xVelocity, yVelocity, 0, maxX, 0, maxY, 0, 0)
                postInvalidateOnAnimation()
            }
        }

        lastTouchX = event.x
        lastTouchY = event.y
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val string = string ?: return

        if (string.isNotEmpty() && lines.isEmpty())
            processString(string)

        val lineHeight = paint.descent() - paint.ascent()
        val linesAbove = max(scrollY / lineHeight, 0f)
        val lineStart = linesAbove.toInt()
        val lineEnd = min(ceil(lineStart + height / lineHeight).toInt(), lines.size)

        val stringY = paddingTop + scrollY - paint.ascent() + (lineStart - linesAbove) * lineHeight
        var x = paddingLeft.toFloat()

        if (isLineNumberVisible) {
            val lineNumberWidth = paint.measureText(lines.size.toString())
            val lineNumberRight = LINE_NUMBER_PADDING + lineNumberWidth
            var y = stringY

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = LINE_NUMBER_TYPEFACE
            paint.color = LINE_NUMBER_COLOR

            for (i in lineStart + 1..lineEnd) {
                canvas.drawText(i.toString(), scrollX + lineNumberRight, y, paint)
                y += lineHeight
            }

            x += lineNumberRight + LINE_NUMBER_PADDING
        }

        val clipLeft = scrollX + x.toInt()
        val clipTop = scrollY
        val clipRight = scrollX + width
        val clipBottom = scrollY + height

        canvas.save()
        canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom)

        var y = stringY

        for (i in lineStart until lineEnd) {
            val line = lines[i]
            formatter.drawLine(canvas, paint, x, y, string, line)
            y += lineHeight
        }

        canvas.restore()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset() && (scroller.currX != scrollX || scroller.currY != scrollY)) {
            scrollTo(scroller.currX, scroller.currY)
            postInvalidateOnAnimation()
        }
    }

    @Suppress("NAME_SHADOWING")
    override fun scrollTo(x: Int, y: Int) {
        val maxX = getMaxScrollX()
        val maxY = getMaxScrollY()
        val x = if (x < 0) 0 else if (x > maxX) maxX else x
        val y = if (y < 0) 0 else if (y > maxY) maxY else y
        super.scrollTo(x, y)
    }

    private fun processString(string: String?) {
        longestLineIndex = -1
        lines.clear()

        if (string == null)
            return

        var start = 0
        var lineVisibleStart = 0
        var isVisibleStartSet = false
        var longestLineLength = 0

        for (i in string.indices) {
            when (string[i]) {
                '\n' -> {
                    lines += LineInfo(start, i, lineVisibleStart)

                    val lineLength = i - start
                    if (lineLength > longestLineLength) {
                        longestLineLength = lineLength
                        longestLineIndex = lines.lastIndex
                    }

                    isVisibleStartSet = false
                    lineVisibleStart = 0
                    start = i + 1
                }
                ' ', '\t' ->
                    Unit
                else -> {
                    if (!isVisibleStartSet) {
                        isVisibleStartSet = true
                        lineVisibleStart = i - start
                    }
                }
            }
        }
    }

    private fun getMaxScrollX(): Int {
        return if (longestLineIndex != -1) {
            val lineInfo = lines[longestLineIndex] // FIXME Crash here when move in history
            val lineWidth = paint.measureText(string, lineInfo.start, lineInfo.end).toInt()
            val maxScroll = lineWidth - width + paddingLeft + paddingRight

            if (isLineNumberVisible)
                maxScroll + paint.measureText(lines.size.toString()).toInt() + LINE_NUMBER_PADDING * 2
            else
                maxScroll
        } else
            0
    }

    private fun getMaxScrollY(): Int {
        val stringHeight = (lines.size * (paint.descent() - paint.ascent())).toInt()
        return stringHeight - height + paddingTop + paddingBottom
    }

    data class LineInfo(
        val start: Int,
        val end: Int,
        val lineVisibleStart: Int,
    )

    interface Formatter {
        fun drawLine(canvas: Canvas, paint: Paint, x: Float, y: Float, string: String, lineInfo: LineInfo)
    }

    companion object {

        private const val LINE_NUMBER_COLOR = 0xff777777.toInt()

        private val LINE_NUMBER_TYPEFACE = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)

        private val LINE_NUMBER_PADDING = dpToPx(5f)
    }
}
