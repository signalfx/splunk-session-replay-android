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

package com.splunk.android.debugger.extension

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.os.Build

@Suppress("DEPRECATION")
internal fun Canvas.clipOutPathCompat(path: Path) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        clipOutPath(path)
    else
        clipPath(path, Region.Op.DIFFERENCE)
}

private val path = Path()

internal fun Canvas.drawRect(rect: RectF, radii: FloatArray?, paint: Paint) {
    if (radii != null) {
        if (radii[0] == radii[2] && radii[2] == radii[4] && radii[4] == radii[6])
            drawRoundRect(rect, radii[0], radii[0], paint)
        else {
            path.addRoundRect(rect, radii, Path.Direction.CW)
            drawPath(path, paint)
            path.reset()
        }
    } else
        drawRect(rect, paint)
}
