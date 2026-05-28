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

import android.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.reflector.Reflector

// MARK Requires Proguard rules

private val reflector = Reflector(4, 0, 0)

internal fun ContentDrawScope.getAndroidCanvas(): Canvas? {
    val scope = this

    return reflector.reflect {
        try {
            val canvasDrawScope = scope.get<Any>("canvasDrawScope") ?: return@reflect null
            val drawParams = canvasDrawScope.get<Any>("drawParams") ?: return@reflect null
            val canvas = drawParams.get<Any>("canvas") ?: return@reflect null
            canvas.get("internalCanvas")
        } catch (e: NoSuchFieldException) {
            Logger.e1("ContentDrawScopeExt_Canvas", "getAndroidCanvas", e)
            null
        }
    }
}
