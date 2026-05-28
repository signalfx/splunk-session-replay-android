package com.splunk.android.common.utils.extensions

fun Long.setFlags(toSet: Long, enable: Boolean): Long {
    return if (enable) {
        this or toSet
    } else {
        this xor (this and toSet)
    }
}

fun Long.areFlagsEnabled(toCheck: Long) = this and toCheck == toCheck
