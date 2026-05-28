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
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.annotation.ColorInt
import com.splunk.android.common.utils.dpToPxF
import com.splunk.android.common.utils.extensions.toArgbHexString
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.common.utils.runOnAndroidAtLeast
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton

private val BITMAP = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
private val CANVAS = Canvas(BITMAP)

private val RUNTIME_SHADER_CLASS = "android.graphics.RuntimeShader".toClass()

internal fun Paint.getDrawColor(dstColor: Int = Color.TRANSPARENT): Int {
    if (RUNTIME_SHADER_CLASS?.isInstance(shader) == true)
        return Color.TRANSPARENT

    return if (shader != null || colorFilter != null || maskFilter != null) {
        val pathEffectBackup = pathEffect
        val strokeCapBackup = strokeCap
        val styleBackup = style

        pathEffect = null
        strokeCap = Paint.Cap.BUTT
        style = Paint.Style.FILL

        BITMAP.setPixel(0, 0, dstColor)
        CANVAS.drawRect(0f, 0f, 1f, 1f, this)

        pathEffect = pathEffectBackup
        strokeCap = strokeCapBackup
        style = styleBackup

        BITMAP.getPixel(0, 0)
    } else
        color
}

internal fun Paint.hasEffect(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        if (blendMode?.let { it != BlendMode.SRC && it != BlendMode.SRC_IN && it != BlendMode.SRC_OUT && it != BlendMode.SRC_OVER } == true)
            return true

    return colorFilter != null || maskFilter != null || pathEffect != null || shader != null
}

internal fun Paint.toPrettyString(): String {
    val blendMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) blendMode else null
    val colorFilter = colorFilter?.let { it::class.java.simpleName }
    val maskFilter = maskFilter?.let { it::class.java.simpleName }
    val pathEffect = pathEffect?.let { it::class.java.simpleName }
    val shader = shader?.let { it::class.java.simpleName }

    return "Paint(color: ${color.toArgbHexString()}, blendMode: $blendMode, colorFilter: $colorFilter, maskFilter: $maskFilter, pathEffect: $pathEffect, shader: $shader, style: $style, strokeWidth: $strokeWidth, textSize: $textSize, textScaleX: $textScaleX)"
}

internal fun Paint.withColor(@ColorInt color: Int, block: (Paint) -> Unit) {
    val originalColor = this.color
    this.color = color
    block(this)
    this.color = originalColor
}

private val SHADOW_THRESHOLD = dpToPxF(5f)

internal fun Paint.getShadowFlag(): Skeleton.Color.Flags.Shadow? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val color = shadowLayerColor

        if (shadowLayerRadius < SHADOW_THRESHOLD || color == 0 || Color.alpha(color) < 25)
            return null

        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        if ((r * 299 + g * 587 + b * 114) / 1000 >= 128)
            Skeleton.Color.Flags.Shadow.LIGHT
        else
            Skeleton.Color.Flags.Shadow.DARK
    } else
        null
}

internal val Paint.underlinePositionCompat: Float
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.Q) { underlinePosition } ?: dpToPxF(1.1f)

internal val Paint.underlineThicknessCompat: Float
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.Q) { underlineThickness } ?: dpToPxF(1f)

internal val Paint.strikeThruPositionCompat: Float
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.Q) { strikeThruPosition } ?: -dpToPxF(4.7f)

internal val Paint.strikeThruThicknessCompat: Float
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.Q) { strikeThruThickness } ?: dpToPxF(0.825f)

internal val Paint.wordSpacingCompat: Float
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.Q) { wordSpacing } ?: 0f

internal val Paint.letterSpacingCompat: Float
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.LOLLIPOP) { letterSpacing } ?: 0f
