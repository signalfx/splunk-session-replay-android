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

import android.graphics.Matrix

// Keep in mind, this is not thread safe

private val values = FloatArray(9)

internal val Matrix.translationX: Float
    get() = getValues()[2]

internal val Matrix.translationY: Float
    get() = getValues()[5]

internal val Matrix.scaleX: Float
    get() = getValues()[0]

internal val Matrix.scaleY: Float
    get() = getValues()[4]

private fun Matrix.getValues(): FloatArray {
    getValues(values)
    return values
}
