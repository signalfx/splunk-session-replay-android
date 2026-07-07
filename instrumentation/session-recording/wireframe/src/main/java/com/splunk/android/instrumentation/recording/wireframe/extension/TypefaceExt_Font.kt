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

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import com.splunk.android.common.utils.runOnAndroidAtLeast
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

private const val DEFAULT_FAMILY_NAME = "sans-serif-medium"

private val systemFontMap = HashMap<Typeface, String>()

@Suppress("UNCHECKED_CAST")
@SuppressLint("DiscouragedPrivateApi")
internal fun Typeface.toFont(): Wireframe.Frame.Scene.Window.View.Skeleton.Text.Font {
    if (systemFontMap.isEmpty()) {
        val field = Typeface::class.java.getDeclaredField("sSystemFontMap")
        field.isAccessible = true

        val map = field.get(null) as Map<String, Typeface>

        for ((family, typeface) in map)
            systemFontMap[typeface] = family
    }

    return Wireframe.Frame.Scene.Window.View.Skeleton.Text.Font(
        familyName = systemFontMap[this] ?: DEFAULT_FAMILY_NAME,
        isItalic = isItalic,
        weight = runOnAndroidAtLeast(Build.VERSION_CODES.P) { weight } ?: if (isBold) 700 else 400
    )
}
