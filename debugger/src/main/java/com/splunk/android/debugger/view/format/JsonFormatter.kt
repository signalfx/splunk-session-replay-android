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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.Keep
import com.splunk.android.debugger.view.format.PrettyCodeView.Formatter
import com.splunk.android.debugger.view.format.PrettyCodeView.LineInfo

@Keep
@Suppress("unused")
internal class JsonFormatter : Formatter { // Used in XML

    private val nameRange = Range()
    private val propertyRange = Range()
    private val emptyRange = Range()

    @Suppress("NAME_SHADOWING")
    override fun drawLine(canvas: Canvas, paint: Paint, x: Float, y: Float, string: String, lineInfo: LineInfo) {
        val line = string.subSequence(lineInfo.start, lineInfo.end)

        val nameRange = getRange(line, NAME_REGEX, lineInfo.lineVisibleStart, nameRange)

        if (nameRange == null) {
            getRange(line, PROPERTY_COLOR_REGEX, lineInfo.lineVisibleStart, propertyRange)?.let {
                drawColorPropertyPart(canvas, paint, x, y, line, emptyRange, it)
                return
            }

            drawText(canvas, paint, line, 0, line.length, x, y, COLOR_NORMAL, TYPEFACE_NORMAL)
        } else {
            getRange(line, PROPERTY_COLOR_REGEX, nameRange.end, propertyRange)?.let {
                drawColorPropertyPart(canvas, paint, x, y, line, nameRange, it)
                return
            }

            getRange(line, PROPERTY_STRING_REGEX, nameRange.end, propertyRange)?.let {
                drawPropertyPart(canvas, paint, x, y, line, nameRange, it, COLOR_PROPERTY_STRING)
                return
            }

            getRange(line, PROPERTY_NUMBER_REGEX, nameRange.end, propertyRange)?.let {
                drawPropertyPart(canvas, paint, x, y, line, nameRange, it, COLOR_PROPERTY_NUMBER)
                return
            }

            getRange(line, PROPERTY_BOOL_REGEX, nameRange.end, propertyRange)?.let {
                drawPropertyPart(canvas, paint, x, y, line, nameRange, it, COLOR_PROPERTY_BOOL)
                return
            }

            val x = drawPropertyNamePart(canvas, paint, x, y, line, nameRange)
            drawText(canvas, paint, line, nameRange.end, line.length, x, y, COLOR_NORMAL, TYPEFACE_NORMAL)
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun drawPropertyNamePart(canvas: Canvas, paint: Paint, x: Float, y: Float, line: CharSequence, nameRange: Range): Float {
        val x = x + drawText(canvas, paint, line, 0, nameRange.start, x, y, COLOR_NORMAL, TYPEFACE_NORMAL)
        return x + drawText(canvas, paint, line, nameRange.start, nameRange.end, x, y, COLOR_NORMAL, TYPEFACE_ITALIC)
    }

    @Suppress("NAME_SHADOWING")
    private fun drawColorPropertyPart(canvas: Canvas, paint: Paint, x: Float, y: Float, line: CharSequence, nameRange: Range, propertyRange: Range) {
        var x = drawPropertyNamePart(canvas, paint, x, y, line, nameRange)

        val color = Color.parseColor(line.substring(propertyRange.start + 1, propertyRange.end - 1))
        val textSize = paint.descent() - paint.ascent()
        val rectSize = textSize * RECT_SCALE
        val offset = textSize - rectSize

        x += drawText(canvas, paint, line, nameRange.end, propertyRange.start, x, y, COLOR_NORMAL, TYPEFACE_NORMAL)
        x += drawText(canvas, paint, line, propertyRange.start, propertyRange.end - 1, x, y, COLOR_PROPERTY_STRING, TYPEFACE_NORMAL)

        val rectLeft = x + offset
        val rectTop = y - textSize + offset
        val rectRight = x + offset + rectSize
        val rectBottom = y - textSize + offset + rectSize

        paint.color = Color.BLACK
        canvas.drawRect(rectLeft - 1, rectTop - 1, rectRight + 1, rectBottom + 1, paint)

        paint.color = color
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint)

        x += textSize

        x += drawText(canvas, paint, line, propertyRange.end - 1, propertyRange.end, x, y, COLOR_PROPERTY_STRING, TYPEFACE_NORMAL)
        drawText(canvas, paint, line, propertyRange.end, line.length, x, y, COLOR_NORMAL, TYPEFACE_NORMAL)
    }

    @Suppress("NAME_SHADOWING")
    private fun drawPropertyPart(canvas: Canvas, paint: Paint, x: Float, y: Float, line: CharSequence, nameRange: Range, propertyRange: Range, color: Int) {
        var x = drawPropertyNamePart(canvas, paint, x, y, line, nameRange)
        x += drawText(canvas, paint, line, nameRange.end, propertyRange.start, x, y, COLOR_NORMAL, TYPEFACE_NORMAL)
        x += drawText(canvas, paint, line, propertyRange.start, propertyRange.end, x, y, color, TYPEFACE_NORMAL)
        drawText(canvas, paint, line, propertyRange.end, line.length, x, y, COLOR_NORMAL, TYPEFACE_NORMAL)
    }

    private fun getRange(line: CharSequence, regex: Regex, start: Int, result: Range): Range? {
        val match = regex.find(line, start)
        val matchRange = match?.groups?.get(1)?.range

        return if (matchRange != null) {
            result.start = matchRange.first
            result.end = matchRange.last + 1
            result
        } else
            null
    }

    private fun drawText(canvas: Canvas, paint: Paint, line: CharSequence, start: Int, end: Int, x: Float, y: Float, color: Int, typeface: Typeface): Float {
        paint.color = color
        paint.typeface = typeface
        paint.textAlign = Paint.Align.LEFT

        canvas.drawText(line, start, end, x, y, paint)
        return paint.measureText(line, start, end)
    }

    private class Range {
        var start = 0
        var end = 0
    }

    companion object {
        private val NAME_REGEX = "\"([^\"#]+)\" ?:".toRegex() // https://regex101.com/r/4AW7Ec/4
        private val PROPERTY_COLOR_REGEX = "(\"#[0-9a-fA-F]{3,}\")".toRegex() // https://regex101.com/r/fNUFjA/4
        private val PROPERTY_STRING_REGEX = ": ?(\"[^\"]*\")".toRegex() // https://regex101.com/r/b4xnjg/3
        private val PROPERTY_NUMBER_REGEX = ": ?(-?[0-9\\.]+)".toRegex() // https://regex101.com/r/gnzDtW/4
        private val PROPERTY_BOOL_REGEX = ": ?(true|false)".toRegex() // https://regex101.com/r/0sESxy/3

        private val TYPEFACE_NORMAL = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        private val TYPEFACE_ITALIC = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)

        private const val COLOR_NORMAL = Color.BLACK
        private const val COLOR_PROPERTY_STRING = 0xff247f1c.toInt()
        private const val COLOR_PROPERTY_NUMBER = 0xfff6191b.toInt()
        private const val COLOR_PROPERTY_BOOL = 0xfff89c1d.toInt()

        private const val RECT_SCALE = 0.7f
    }
}
