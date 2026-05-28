package com.splunk.android.common.utils.extensions

fun Int.toArgbHexString(): String {
    return "#%08X".format(0xffffffff.toInt() and this)
}
