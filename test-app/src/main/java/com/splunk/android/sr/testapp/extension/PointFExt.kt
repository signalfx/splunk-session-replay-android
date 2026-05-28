package com.splunk.android.sr.testapp.extension

import android.graphics.PointF
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val ZERO = PointF(0f, 0f)

fun PointF.rotate(angle: Float, pivot: PointF = ZERO): PointF {
    val angleRad = angle * PI.toFloat() / 180f

    val cosTheta = cos(angleRad)
    val sinTheta = sin(angleRad)

    val tempX = cosTheta * (x - pivot.x) - sinTheta * (y - pivot.y) + pivot.x
    val tempY = sinTheta * (x - pivot.x) + cosTheta * (y - pivot.y) + pivot.y

    x = tempX
    y = tempY

    return this
}

fun PointF.add(offsetX: Float, offsetY: Float): PointF {
    x += offsetX
    y += offsetY

    return this
}
