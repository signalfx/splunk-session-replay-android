package com.splunk.android.debugger.extension

import android.view.ViewGroup

internal fun <T : ViewGroup> T.withDisabledAnimations(block: (T) -> Unit) {
    val temp = layoutTransition
    layoutTransition = null
    block(this)
    layoutTransition = temp
}
