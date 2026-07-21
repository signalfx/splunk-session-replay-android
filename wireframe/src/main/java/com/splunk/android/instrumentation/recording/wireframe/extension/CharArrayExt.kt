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

package com.splunk.android.instrumentation.recording.wireframe.extension

internal fun CharArray.getFirstNonWhitespaceCharIndex(index: Int = 0, count: Int = size): Int? {
    for (i in index until index + count)
        if (!get(i).isWhitespace())
            return i

    return null
}

@Suppress("UseWithIndex")
internal fun CharArray.getLastWhitespaceCharCount(index: Int = 0, count: Int = size): Int {
    val end = index + count
    var whitespaceCount = 0

    for (i in (index until end).reversed()) {
        if (!get(i).isWhitespace())
            return whitespaceCount

        whitespaceCount++
    }

    return count
}
