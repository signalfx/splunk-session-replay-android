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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.plusAssign
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.CompoundButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class SwitchCompatDescriptor : CompoundButtonDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.SwitchCompat".toClass()

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is SwitchCompat)
            return

        result += view.trackDrawableSafe?.getSkeleton()
        result += view.thumbDrawableSafe?.getSkeleton()
    }

    private val SwitchCompat.trackDrawableSafe: Drawable?
        get() = try {
            trackDrawable
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "getTrackDrawableSafe", e)
            null
        }

    private val SwitchCompat.thumbDrawableSafe: Drawable?
        get() = try {
            thumbDrawable
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "getThumbDrawableSafe", e)
            null
        }

    private companion object {
        const val TAG = "SwitchCompatDescriptor"
    }
}
