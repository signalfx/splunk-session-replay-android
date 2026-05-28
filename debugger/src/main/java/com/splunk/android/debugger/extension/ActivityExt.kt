package com.splunk.android.debugger.extension

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.splunk.android.common.utils.extensions.contentView
import com.splunk.android.common.utils.extensions.get
import kotlin.math.max

internal fun Activity.getContentPadding(): Padding? {
    val decorView = window.decorView as ViewGroup
    val contentView = contentView ?: return null

    if (!ViewCompat.isAttachedToWindow(contentView))
        return null

    val offset = contentView.getOffsetRelativeToParent(decorView)

    if (offset.x == 0 && offset.y == 0) {
        val view = decorView.findViewById<View>(android.R.id.statusBarBackground)

        if (view != null && view.visibility == View.VISIBLE)
            offset.y = view.height
    }

    val left = offset.x
    val top = offset.y
    val right = decorView.width - contentView.width - left
    val bottom = decorView.height - contentView.height - top

    return Padding(left, top, right, bottom)
}

internal data class Padding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

private val INSETS_REGEX = "(systemWindowInsets|stableInsets)=Rect\\((\\d+), (\\d+) - (\\d+), (\\d+)".toRegex() // https://regex101.com/r/gpFjZK/1

internal fun Activity.getKeyboardHeight(): Int { // FIXME This is not correct. Use solution from KeyboardConsumer.
    val contentView = contentView ?: return 0

    var stableInsetBottom = 0
    var systemWindowInsetBottom = 0

    try {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
                val mAttachInfo = contentView.get<Any>("mAttachInfo") ?: return 0
                val mContentInsets = mAttachInfo.get<Rect>("mContentInsets") ?: return 0
                val mStableInsets = mAttachInfo.get<Rect>("mStableInsets") ?: return 0

                stableInsetBottom = mStableInsets.bottom
                systemWindowInsetBottom = mContentInsets.bottom
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> {
                val windowInsets = contentView.rootWindowInsets ?: return 0
                val matches = INSETS_REGEX.findAll(windowInsets.toString())

                for (match in matches) {
                    val size = match.groupValues[5].toInt()

                    when (match.groupValues[1]) {
                        "stableInsets" ->
                            stableInsetBottom = size
                        "systemWindowInsets" ->
                            systemWindowInsetBottom = size
                    }
                }
            }
            else -> {
                val windowInsets = contentView.rootWindowInsets ?: return 0
                stableInsetBottom = windowInsets.stableInsetBottom
                systemWindowInsetBottom = windowInsets.systemWindowInsetBottom
            }
        }
    } catch (_: Exception) {
    }

    return max(0, systemWindowInsetBottom - stableInsetBottom)
}
