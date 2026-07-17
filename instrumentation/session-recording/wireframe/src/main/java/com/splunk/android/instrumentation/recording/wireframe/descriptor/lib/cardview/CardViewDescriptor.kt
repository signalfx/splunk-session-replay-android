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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.cardview

import android.view.View
import androidx.cardview.widget.CardView
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.dpToPxF
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.Shadow

// FIXME foreground bounds

/* MARK
 *  - Compatible with androidx.cardview:cardview:1.0.0
 */
internal open class CardViewDescriptor : FrameLayoutDescriptor() {

    private val canvas = SkeletonCanvas()

    override val intendedClass: Class<*>? = "androidx.cardview.widget.CardView".toClass()

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Skeleton>) {
        if (view !is CardView)
            return

        view.background.draw(canvas)

        result += if (view.cardElevationSafe >= SHADOW_THRESHOLD)
            canvas.skeletons.map { if (it is Skeleton.Color) it.copy(flags = Skeleton.Color.Flags(Shadow.DARK)) else it }
        else
            canvas.skeletons

        canvas.skeletons.clear()
    }

    private val CardView.cardElevationSafe: Float
        get() = try {
            cardElevation
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "getCardElevationSafe", e)
            0f
        }

    private companion object {
        const val TAG = "CardViewDescriptor"

        val SHADOW_THRESHOLD = dpToPxF(5f)
    }
}
