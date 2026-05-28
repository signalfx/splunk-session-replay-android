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
