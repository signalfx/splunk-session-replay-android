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

package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

private val bounds = RectF()

internal fun Path.toBitmap(): Bitmap? {
    if (isEmpty)
        return null

    val rect = RectF()
    computeBounds(rect, true)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = Color.WHITE

    val bitmap = Bitmap.createBitmap(rect.right.toInt(), rect.bottom.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawPath(this, paint)

    return bitmap
}

internal fun Path.toPrettyString(): String {
    computeBounds(bounds, false)
    return "Path(bounds: $bounds)"
}
