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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.viewpager

import android.graphics.Point
import android.view.View
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

/* FIXME
 *  - Check support library
 */
internal open class ViewPagerDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.viewpager.widget.ViewPager".toClass()

    override fun getScrollOffset(view: View): Point? {
        return Point(view.scrollX, view.scrollY)
    }
}
