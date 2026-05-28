package com.splunk.android.instrumentation.recording.wireframe.util

import com.splunk.android.common.logger.Logger

// MARK Required Proguard rules

object ComposeInfo {

    private const val TAG = "ComposeInfo"

    val version: Version by lazy {
        when {
            hasClass("androidx.compose.ui.node.UnplacedAwareModifierNode") ->
                VERSION_1_10
            hasClass("androidx.compose.ui.node.SortedSet") ->
                VERSION_1_9
            hasClass("androidx.compose.ui.node.DistanceAndFlags") ->
                VERSION_1_8
            hasClass("androidx.compose.ui.graphics.layer.GraphicsLayer") ->
                VERSION_1_7
            hasClass("androidx.compose.ui.platform.PlatformTextInputModifierNode") ->
                VERSION_1_6
            hasClass("androidx.compose.ui.node.CompositionLocalConsumerModifierNode") ->
                VERSION_1_5
            hasClass("androidx.compose.ui.focus.FocusOwner") ->
                VERSION_1_4
            hasClass("androidx.compose.ui.node.DelegatableNode") ->
                VERSION_1_3
            hasClass("androidx.compose.ui.text.PlatformTextStyle") ->
                VERSION_1_2
            else -> {
                Logger.e1(TAG, "version - unable to detect Compose version")
                VERSION_UNKNOWN
            }
        }
    }

    private fun hasClass(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}

val VERSION_UNKNOWN = Version(0, 0)
val VERSION_1_2 = Version(1, 2)
val VERSION_1_3 = Version(1, 3)
val VERSION_1_4 = Version(1, 4)
val VERSION_1_5 = Version(1, 5)
val VERSION_1_6 = Version(1, 6)
val VERSION_1_7 = Version(1, 7)
val VERSION_1_8 = Version(1, 8)
val VERSION_1_9 = Version(1, 9)
val VERSION_1_10 = Version(1, 10)
