package com.splunk.android.common.utils.extensions

import android.view.View
import android.view.ViewGroup

val ViewGroup.children: Sequence<View>
    get() = object : Sequence<View> {
        override fun iterator(): Iterator<View> = this@children.iterator()
    }

fun ViewGroup.iterator(): Iterator<View> {
    return object : Iterator<View> {
        private var index = 0

        override fun hasNext(): Boolean = index < childCount
        override fun next(): View = getChildAt(index++) ?: throw IndexOutOfBoundsException()
    }
}
