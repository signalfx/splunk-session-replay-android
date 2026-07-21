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
import android.view.MotionEvent
import android.widget.FrameLayout
import com.splunk.rum.common.utils.dpToPx
import kotlin.math.abs

internal class TouchDispatchFrameLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var downTime = 0L
    private var downX = 0f
    private var downY = 0f

    private var onClickListener: OnClickListener? = null

    var listener: Listener? = null

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (onClickListener == null)
            return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downTime = System.currentTimeMillis()
                downX = event.x
                downY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(event.x - downX) >= CLICK_THRESHOLD || abs(event.y - downY) >= CLICK_THRESHOLD)
                    downTime = 0L
            }
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - downTime < TIME_THRESHOLD)
                    if (abs(event.x - downX) < CLICK_THRESHOLD && abs(event.y - downY) < CLICK_THRESHOLD)
                        onClickListener?.onClick(this)
            }
        }

        return super.onInterceptTouchEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return super.dispatchTouchEvent(event) || (listener?.dispatchTouchEvent(this, context, event) ?: false)
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
        onClickListener = listener
    }

    interface Listener {
        fun dispatchTouchEvent(view: TouchDispatchFrameLayout, context: Context, event: MotionEvent): Boolean
    }

    companion object {
        private const val TIME_THRESHOLD = 250L
        private val CLICK_THRESHOLD = dpToPx(20f)
    }
}
