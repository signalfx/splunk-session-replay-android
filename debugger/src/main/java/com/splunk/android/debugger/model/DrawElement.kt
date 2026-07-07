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
