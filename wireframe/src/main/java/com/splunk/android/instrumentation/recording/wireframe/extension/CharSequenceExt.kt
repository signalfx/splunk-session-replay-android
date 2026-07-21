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

internal fun CharSequence.isBlank(startIndex: Int = 0, endIndex: Int = length): Boolean {
    for (i in startIndex until endIndex)
        if (!get(i).isWhitespace())
            return false

    return true
}

internal fun CharSequence.getFirstNonWhitespaceCharIndex(startIndex: Int = 0, endIndex: Int = length): Int? {
    for (i in startIndex until endIndex)
        if (!get(i).isWhitespace())
            return i

    return null
}

internal fun CharSequence.getLastNonWhitespaceCharIndex(startIndex: Int = 0, endIndex: Int = length): Int? {
    for (i in (startIndex until endIndex).reversed())
        if (!get(i).isWhitespace())
            return i + 1

    return null
}
