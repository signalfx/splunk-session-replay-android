package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.core

import android.graphics.Point
import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

/* FIXME
 *  - Check support library
 */
internal open class NestedScrollViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.core.widget.NestedScrollView".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun getScrollOffset(view: View): Point {
        return Point(view.scrollX, view.scrollY)
    }
}
