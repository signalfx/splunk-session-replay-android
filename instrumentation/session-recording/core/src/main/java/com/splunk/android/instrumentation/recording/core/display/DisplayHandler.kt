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
