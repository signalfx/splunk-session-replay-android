package com.splunk.android.sr.testapp.view.bridge

import android.graphics.Rect

sealed interface TomasElement {

    val isSensitive: Boolean

    data class Rectangle(val rect: Rect, val color: Int, override val isSensitive: Boolean) : TomasElement

    data class GradientRectangle(val rect: Rect, val topLeftColor: Int, val bottomRightColor: Int, override val isSensitive: Boolean) : TomasElement

    data class Circle(val x: Int, val y: Int, val radius: Int, val color: Int, override val isSensitive: Boolean) : TomasElement

    data class Text(val x: Int, val y: Int, val text: String, val color: Int, val size: Float, override val isSensitive: Boolean) : TomasElement
}
