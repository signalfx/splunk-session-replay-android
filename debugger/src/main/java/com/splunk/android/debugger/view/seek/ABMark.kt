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
