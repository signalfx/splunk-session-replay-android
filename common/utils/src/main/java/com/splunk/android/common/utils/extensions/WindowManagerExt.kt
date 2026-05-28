package com.splunk.android.common.utils.extensions

import android.view.WindowManager

fun WindowManager.LayoutParams.hasDimBehind(): Boolean {
    return (flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND) == WindowManager.LayoutParams.FLAG_DIM_BEHIND && dimAmount > 0f
}
