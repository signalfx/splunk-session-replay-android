package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.viewpager

import android.graphics.Point
import android.view.View
import com.splunk.android.common.utils.extensions.toClass
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
