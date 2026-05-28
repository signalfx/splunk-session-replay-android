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
