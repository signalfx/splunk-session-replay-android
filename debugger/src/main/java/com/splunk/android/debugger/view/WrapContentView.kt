package com.splunk.android.debugger.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

internal abstract class WrapContentView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = if (widthMode == MeasureSpec.UNSPECIFIED) Int.MAX_VALUE else MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = if (heightMode == MeasureSpec.UNSPECIFIED) Int.MAX_VALUE else MeasureSpec.getSize(heightMeasureSpec)

        val measuredWidth: Int
        val measuredHeight: Int

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) { // FIXME Android 5 require -3 to wrap_content, why?
            measuredWidth = widthSize
            measuredHeight = heightSize
        } else {
            val aspectRatio = getContentAspectRatio()

            if (aspectRatio == 0f) {
                measuredWidth = 0
                measuredHeight = 0
            } else {
                if (heightMode == MeasureSpec.EXACTLY) {
                    measuredWidth = min(widthSize, (heightSize * aspectRatio).toInt())
                    measuredHeight = (measuredWidth / aspectRatio).toInt()
                } else if (widthMode == MeasureSpec.EXACTLY) {
                    measuredHeight = min(heightSize, (widthSize / aspectRatio).toInt())
                    measuredWidth = (measuredHeight * aspectRatio).toInt()
                } else {
                    if (widthSize > heightSize * aspectRatio) {
                        measuredHeight = heightSize
                        measuredWidth = (measuredHeight * aspectRatio).toInt()
                    } else {
                        measuredWidth = widthSize
                        measuredHeight = (measuredWidth / aspectRatio).toInt()
                    }
                }
            }
        }

        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    protected abstract fun getContentAspectRatio(): Float
}
