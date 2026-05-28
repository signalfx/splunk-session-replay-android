package com.splunk.android.common.utils.extensions

import android.graphics.Color

val Int.a: Int
    get() = Color.alpha(this)

val Int.r: Int
    get() = Color.red(this)

val Int.g: Int
    get() = Color.green(this)

val Int.b: Int
    get() = Color.blue(this)
