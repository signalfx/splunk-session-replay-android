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

package com.splunk.android.debugger.extension

import android.content.res.TypedArray
import androidx.annotation.StyleableRes

internal inline fun <reified T : Enum<T>> TypedArray.getEnum(@StyleableRes index: Int, defValue: T?): T? {
    val ordinal = getInt(index, defValue?.ordinal ?: -1)
    return if (ordinal == -1)
        defValue
    else
        enumValues<T>()[ordinal]
}
