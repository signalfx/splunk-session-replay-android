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

package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.CompoundButton
import com.splunk.rum.common.utils.extensions.plusAssign
import com.splunk.android.instrumentation.recording.wireframe.extension.buttonDrawableCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class CompoundButtonDescriptor : ButtonDescriptor() {

    override val intendedClass: Class<*>? = CompoundButton::class.java

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is CompoundButton)
            return

        result += view.buttonDrawableCompat?.getSkeleton()
    }
}
