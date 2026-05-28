package com.splunk.android.common.utils

import android.content.res.Resources
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

private val density: Float = Resources.getSystem().displayMetrics.density

/**
 * Perform [block] when Android version is at least [versionCode].
 *
 * @param versionCode Android SDK version code from [Build.VERSION_CODES]
 */
@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
inline fun <T> runOnAndroidAtLeast(versionCode: Int, crossinline block: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= versionCode) block() else null
}

fun dpToPx(dp: Float): Int = (dp * density + 0.5f).toInt()

fun dpToPxF(dp: Float): Float = dp * density

fun pxToDp(px: Int): Float = px / density
