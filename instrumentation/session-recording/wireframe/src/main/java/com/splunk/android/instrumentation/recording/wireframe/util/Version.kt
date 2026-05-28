package com.splunk.android.instrumentation.recording.wireframe.util

@JvmInline
value class Version(val value: Int) : Comparable<Version> {

    constructor(major: Int, minor: Int) : this((major shl 28) or (minor and 0x0FFFFFFF))

    val major: Int
        get() = value ushr 28

    val minor: Int
        get() = value and 0x0FFFFFFF

    override fun compareTo(other: Version): Int {
        return compareValuesBy(this, other, { it.major }, { it.minor })
    }

    override fun toString(): String = "$major.$minor"
}
