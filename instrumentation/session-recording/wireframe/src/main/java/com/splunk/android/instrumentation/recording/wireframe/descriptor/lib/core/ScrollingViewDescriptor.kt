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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.core

import android.graphics.Point
import android.view.View
import androidx.core.view.ScrollingView
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

/* FIXME
 *  - Check RecyclerView from support library
 */
internal open class ScrollingViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.core.view.ScrollingView".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun getScrollOffset(view: View): Point? {
        if (view !is ScrollingView)
            return super.getScrollOffset(view)

        return try {
            val x = view.computeHorizontalScrollOffset()
            val y = view.computeVerticalScrollOffset()

            Point(x, y)
        } catch (e: Throwable) {
            Logger.e1(TAG, "getScrollOffset", e)
            super.getScrollOffset(view)
        }
    }

    override fun useScrollOffsetForChildren(view: View): Boolean {
        return false
    }

    private companion object {
        const val TAG = "ScrollingViewDescriptor"
    }
}
