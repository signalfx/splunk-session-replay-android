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
