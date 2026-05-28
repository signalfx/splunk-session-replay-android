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
