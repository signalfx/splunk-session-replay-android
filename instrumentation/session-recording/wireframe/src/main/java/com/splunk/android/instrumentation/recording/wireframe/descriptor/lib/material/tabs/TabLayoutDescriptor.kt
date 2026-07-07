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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.tabs

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.children
import com.splunk.android.common.utils.extensions.get
import com.splunk.android.common.utils.extensions.plusAssign
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.HorizontalScrollViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import com.google.android.material.tabs.TabLayout

/* MARK
 *  - Compatible with com.google.android.material:material of version 1.2.0 and 1.3.0
 *  - Requires Proguard rules
 */
internal open class TabLayoutDescriptor : HorizontalScrollViewDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.tabs.TabLayout".toClass()

    override fun getType(view: View): Window.View.Type? {
        return Window.View.Type.TAP_BAR
    }

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is TabLayout)
            return

        try {
            val slidingTabIndicator = view.getChildAt(0) as ViewGroup

            for (tab in slidingTabIndicator.children)
                if (tab is TabLayout.TabView) {
                    val backgroundDrawable = tab.get<Drawable>("baseBackgroundDrawable") ?: continue
                    val skeleton = backgroundDrawable.getSkeleton() ?: continue

                    result += skeleton
                }
        } catch (e: Exception) {
            Logger.e1(TAG, "getSkeletons", e)
        }
    }

    private companion object {
        const val TAG = "TabLayoutDescriptor"
    }
}
