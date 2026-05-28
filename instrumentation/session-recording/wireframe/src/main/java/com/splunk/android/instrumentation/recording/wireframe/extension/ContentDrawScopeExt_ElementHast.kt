package com.splunk.android.instrumentation.recording.wireframe.extension

import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.reflector.Reflector
import com.splunk.android.instrumentation.recording.wireframe.util.ComposeInfo
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_3

// MARK Requires Proguard rules

private val reflector = Reflector(3, 0, 0)

internal fun ContentDrawScope.getElementHash(): Int {
    val scope = this

    return reflector.reflect {
        try {
            when {
                ComposeInfo.version >= VERSION_1_3 -> {
                    val drawNode = scope.get<Any>("drawNode")?.get<Any>("coordinator")?.get<Any>("layoutNode")
                    System.identityHashCode(drawNode)
                }
                else -> {
                    val drawEntity = scope.get<Any>("drawEntity")?.get<Any>("layoutNodeWrapper")?.get<Any>("layoutNode")
                    System.identityHashCode(drawEntity)
                }
            }
        } catch (e: NoSuchFieldException) {
            Logger.e1("ContentDrawScopeExt_ElementHash", "getElementHash", e)
            0
        }
    }
}
