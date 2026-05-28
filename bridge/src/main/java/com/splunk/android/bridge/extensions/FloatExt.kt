package com.splunk.android.bridge.extensions

import com.splunk.android.common.utils.dpToPx

/**
 * Converts dp to px. Do not remove, used by bridge.
 */
val Float.px: Int
    get() = dpToPx(this)
