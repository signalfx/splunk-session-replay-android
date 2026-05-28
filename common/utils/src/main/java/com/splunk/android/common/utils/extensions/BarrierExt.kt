package com.splunk.android.common.utils.extensions

import com.splunk.android.common.utils.Barrier

inline fun barrier(count: Int, crossinline block: (Barrier) -> Unit) {
    val barrier = Barrier(count)
    block(barrier)
    barrier.waitToComplete()
}
