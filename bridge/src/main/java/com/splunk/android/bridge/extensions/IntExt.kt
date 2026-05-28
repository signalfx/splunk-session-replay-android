package com.splunk.android.bridge.extensions

import com.splunk.android.common.utils.pxToDp

/**
 * Converts px to dp. Do not remove, used by bridge.
 */
val Int.dp: Float
    get() = pxToDp(this)
