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
