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

package com.splunk.android.debugger.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.splunk.android.common.utils.extensions.children
import com.splunk.android.debugger.R
import kotlin.math.cos
import kotlin.math.sin

internal class WheelLayout(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    var angleStart = 0f
        set(value) {
            if (value > 360f)
                throw IllegalArgumentException()

            field = value
            requestLayout()
        }

    var angleEnd = 360f
        set(value) {
            if (value > 360f)
                throw IllegalArgumentException()

            field = value
            requestLayout()
        }

    var radius = 0f
        set(value) {
            field = value
            requestLayout()
        }

    var centerOffsetX = 0f
        set(value) {
            field = value
            requestLayout()
        }

    var centerOffsetY = 0f
        set(value) {
            field = value
            requestLayout()
        }

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.WheelLayout)
            angleStart = a.getFloat(R.styleable.WheelLayout_wheel_angleStart, angleStart)
            angleEnd = a.getFloat(R.styleable.WheelLayout_wheel_angleEnd, angleEnd)
            radius = a.getDimension(R.styleable.WheelLayout_wheel_radius, radius)
            centerOffsetX = a.getDimension(R.styleable.WheelLayout_wheel_centerOffsetX, centerOffsetX)
            centerOffsetY = a.getDimension(R.styleable.WheelLayout_wheel_centerOffsetY, centerOffsetY)
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val left = left + paddingLeft
        val top = top + paddingTop
        val right = right - paddingRight
        val bottom = bottom - paddingBottom

        val width = right - left
        val height = bottom - top

        val centerX = paddingLeft + width / 2 + centerOffsetX.toInt()
        val centerY = paddingRight + height / 2 + centerOffsetY.toInt()

        layoutChildren(centerX, centerY)
    }

    private fun layoutChildren(cx: Int, cy: Int) {
        val childCount = children.count { it.visibility != GONE }
        val angleIncrement = (angleEnd - angleStart) / (childCount - 1)

        val angleIncrementRad = -angleIncrement * Math.PI / 180
        var currentAngleRad = -angleStart * Math.PI / 180

        for (child in children) {
            if (child.visibility == GONE)
                continue

            val childCenterX = (radius * cos(currentAngleRad)).toInt()
            val childCenterY = (radius * sin(currentAngleRad)).toInt()

            val left = cx + childCenterX - child.measuredWidth / 2
            val top = cy - childCenterY - child.measuredHeight / 2
            val right = left + child.measuredWidth
            val bottom = top + child.measuredHeight

            child.layout(left, top, right, bottom)

            currentAngleRad += angleIncrementRad
        }
    }
}
