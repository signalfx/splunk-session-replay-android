/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
