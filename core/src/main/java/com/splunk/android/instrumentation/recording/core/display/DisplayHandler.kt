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

package com.splunk.android.instrumentation.recording.core.display

import android.app.Activity
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.view.Surface
import com.splunk.android.instrumentation.recording.core.extensions.displayCompat

internal object DisplayHandler : IDisplayHandler {
    private var realDisplayWidthPx: Float? = null
    private var realDisplayHeightPx: Float? = null
    private var isWideScreen = false

    override fun setupRealDisplaySize(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && realDisplayWidthPx == null || realDisplayHeightPx == null) {
            val displaySize = Point()
            val defaultDisplay = activity.displayCompat ?: return // No display no size
            defaultDisplay.getRealSize(displaySize)

            val width: Int
            val height: Int

            when (defaultDisplay.rotation) {
                Surface.ROTATION_0, Surface.ROTATION_180 -> {
                    width = displaySize.x
                    height = displaySize.y
                }
                Surface.ROTATION_90, Surface.ROTATION_270 -> {
                    width = displaySize.y
                    height = displaySize.x
                }
                else -> return
            }

            realDisplayWidthPx = width.toFloat()
            realDisplayHeightPx = height.toFloat()
            isWideScreen = width > height
        }
    }

    override fun longerDisplaySideSize(): Float = kotlin.math.max(displayWidthPx(), displayHeightPx())

    override fun displayWidthPx(): Float {
        return realDisplayWidthPx ?: Resources.getSystem().displayMetrics.widthPixels.toFloat()
    }

    override fun displayHeightPx(): Float {
        return realDisplayHeightPx ?: Resources.getSystem().displayMetrics.heightPixels.toFloat()
    }

    override fun getScreenDensity(): Float {
        return Resources.getSystem().displayMetrics.density
    }
}
