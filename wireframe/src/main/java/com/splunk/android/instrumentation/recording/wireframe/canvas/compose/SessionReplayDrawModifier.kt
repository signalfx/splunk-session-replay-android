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

package com.splunk.android.instrumentation.recording.wireframe.canvas.compose

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import com.splunk.android.instrumentation.recording.wireframe.extension.getAndroidCanvas
import com.splunk.android.instrumentation.recording.wireframe.extension.getAndroidView
import com.splunk.android.instrumentation.recording.wireframe.extension.getElementHash
import com.splunk.android.instrumentation.recording.wireframe.util.DesugaringSafeModifierElement

/**
 * A [DrawModifier] for Jetpack Compose that facilitates session replay recording.
 *
 * This modifier intercepts the drawing process of a Composable to capture wireframe data.
 * It identifies whether the content is a native Composable or a classic Android [android.view.View]
 * embedded within Compose.
 *
 * For native Composables, it notifies the recording [ComposeCanvas] about the start and end
 * of the element's drawing, providing metadata such as an optional [id] and a [isSensitive] flag.
 *
 * For embedded Android [android.view.View]s, it passes the view instance directly to the
 * [ComposeCanvas] along with this modifier's metadata.
 *
 * This process allows the session recording framework to build a wireframe representation
 * of the UI for later replay and analysis.
 *
 * Note: This implementation uses reflection to access internal, non-public APIs of the Compose
 * framework, which is indicated by the extension functions it uses.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
@SuppressLint("ModifierFactoryReturnType")
data class SessionReplayDrawModifier(
    internal val id: String?,
    internal val isSensitive: Boolean?
) : DesugaringSafeModifierElement(), DrawModifier {

    override fun ContentDrawScope.draw() {
        val canvas = getAndroidCanvas() as? ComposeCanvas
        val modifier = this@SessionReplayDrawModifier

        if (canvas != null) {
            val androidView = getAndroidView()

            if (androidView != null)
                canvas.addViewModifier(androidView, modifier)
            else {
                val elementHash = getElementHash()

                canvas.beginComposeElement(modifier, elementHash)
                drawContent()
                canvas.endComposeElement()
            }
        } else
            drawContent()
    }
}
