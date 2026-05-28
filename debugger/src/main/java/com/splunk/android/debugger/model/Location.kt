package com.splunk.android.debugger.model

import android.annotation.SuppressLint
import android.view.Gravity

internal enum class Location {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

    @SuppressLint("RtlHardcoded")
    fun toGravity(): Int {
        return when (this) {
            TOP_LEFT ->
                Gravity.TOP or Gravity.LEFT
            TOP_RIGHT ->
                Gravity.TOP or Gravity.RIGHT
            BOTTOM_LEFT ->
                Gravity.BOTTOM or Gravity.LEFT
            BOTTOM_RIGHT ->
                Gravity.BOTTOM or Gravity.RIGHT
        }
    }
}
