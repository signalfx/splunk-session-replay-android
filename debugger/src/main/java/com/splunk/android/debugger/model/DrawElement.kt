package com.splunk.android.debugger.model

import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.splunk.android.common.utils.Colors

internal sealed interface DrawElement {

    val rect: RectF
    val clipRect: Rect?

    data class Color(
        override val rect: RectF,
        override val clipRect: Rect?,
        val radii: FloatArray?,
        val colors: Colors,
        val alpha: Int,
        val shadow: Shadow?
    ) : DrawElement {

        enum class Shadow { DARK, LIGHT }
    }

    data class Text(
        override val rect: RectF,
        override val clipRect: Rect?,
        val text: CharSequence,
        val color: Int,
        val size: Float,
        val typeface: Typeface,
        val letterSpacing: Float
    ) : DrawElement

    data class ViewBorder(
        override val rect: RectF,
        override val clipRect: Rect,
    ) : DrawElement
}
