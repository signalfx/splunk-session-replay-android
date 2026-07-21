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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.appbar

import android.graphics.drawable.Drawable
import android.view.View
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.extensions.get
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import com.google.android.material.appbar.AppBarLayout

/* MARK
 *  - Compatible with com.google.android.material:material:1.2.0
 *  - Requires Proguard rules
 */
internal open class AppBarLayoutDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.appbar.AppBarLayout".toClass()

    override fun getForegroundSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getForegroundSkeletons(view, isSensitive, result)

        if (view !is AppBarLayout)
            return

        getStatusBarForeground(view, result)
    }

    private fun getStatusBarForeground(view: AppBarLayout, result: MutableList<Window.View.Skeleton>) {
        try {
            val statusBarForeground = view.statusBarForegroundSafe ?: return
            val currentOffset = view.get<Int>("currentOffset") ?: error("Property 'AppBarLayout.currentOffset' not found")

            val skeleton = statusBarForeground.getSkeleton() ?: return
            skeleton.rect.offset(0, -currentOffset)

            result += skeleton
        } catch (e: Exception) {
            Logger.e1(TAG, "getStatusBarForeground", e)
        }
    }

    private val AppBarLayout.statusBarForegroundSafe: Drawable?
        get() = try {
            statusBarForeground
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "getStatusBarForegroundSafe", e)
            null
        }

    private companion object {
        const val TAG = "AppBarLayoutDescriptor"
    }
}
