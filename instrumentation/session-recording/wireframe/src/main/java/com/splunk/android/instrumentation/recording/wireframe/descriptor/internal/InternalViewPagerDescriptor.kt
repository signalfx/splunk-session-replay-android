package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import android.graphics.Point
import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class InternalViewPagerDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "com.android.internal.widget.ViewPager".toClass()

    override fun getScrollOffset(view: View): Point? {
        return Point(view.scrollX, view.scrollY)
    }
}
