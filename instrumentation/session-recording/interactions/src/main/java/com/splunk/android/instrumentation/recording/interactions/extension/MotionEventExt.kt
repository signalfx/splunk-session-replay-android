package com.splunk.android.instrumentation.recording.interactions.extension

import android.view.MotionEvent

internal val MotionEvent.pointerId: Int
    get() = getPointerId(actionIndex)

internal val MotionEvent.actionX: Float
    get() = getX(actionIndex)

internal val MotionEvent.actionY: Float
    get() = getY(actionIndex)

internal val MotionEvent.isTouchUp: Boolean
    get() = action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_UP
