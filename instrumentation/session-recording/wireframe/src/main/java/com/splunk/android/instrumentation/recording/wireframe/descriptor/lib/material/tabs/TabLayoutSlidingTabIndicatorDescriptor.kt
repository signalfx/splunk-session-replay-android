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

import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.Colors
import com.splunk.android.common.utils.extensions.plusAssign
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.common.utils.reflector.Reflector
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

// MARK Requires Proguard rules

internal open class TabLayoutSlidingTabIndicatorDescriptor : LinearLayoutDescriptor() {

    private val reflector = Reflector(3, 0, 0)

    override val intendedClass: Class<*>? = "com.google.android.material.tabs.TabLayout\$SlidingTabIndicator".toClass()

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is ViewGroup)
            return

        result += getIndicatorSkeleton(view)
    }

    @Suppress("USELESS_ELVIS", "ConvertTwoComparisonsToRangeCheck")
    private fun getIndicatorSkeleton(view: ViewGroup): Window.View.Skeleton? {
        try {
            val tabLayout = view.parent as? TabLayout ?: return null
            val tabSelectedIndicator = tabLayout.tabSelectedIndicator ?: return null // Can be null

            var indicatorLeft: Int
            var indicatorRight: Int
            var selectedIndicatorColor: Int

            reflector.reflect {
                try { // v1.2.0
                    indicatorLeft = view.get("indicatorLeft") ?: throwIncompatibleVersion()
                    indicatorRight = view.get("indicatorRight") ?: throwIncompatibleVersion()
                    selectedIndicatorColor = view.get<Paint>("selectedIndicatorPaint")?.color ?: throwIncompatibleVersion()
                } catch (_: Exception) {
                    try { // v1.3.0+
                        indicatorLeft = tabSelectedIndicator.bounds.left
                        indicatorRight = tabSelectedIndicator.bounds.right
                        selectedIndicatorColor = tabLayout.get("tabSelectedIndicatorColor") ?: throwIncompatibleVersion()
                    } catch (_: Exception) {
                        throw IllegalStateException("Incompatible version of ${tabLayout::class.java.simpleName}")
                    }
                }
            }

            if (indicatorLeft < 0 || indicatorRight <= indicatorLeft || selectedIndicatorColor == Color.TRANSPARENT)
                return null

            val skeleton = tabSelectedIndicator.getSkeleton() ?: return null

            return if (skeleton is Window.View.Skeleton.Color) skeleton.copy(colors = Colors(selectedIndicatorColor)) else skeleton
        } catch (e: Exception) {
            Logger.e1(TAG, "getIndicatorSkeleton", e)
        }

        return null
    }

    private fun throwIncompatibleVersion(): Nothing {
        throw IllegalStateException("Incompatible version")
    }

    private companion object {
        const val TAG = "TabLayoutSlidingTabIndicatorDescriptor"
    }
}
