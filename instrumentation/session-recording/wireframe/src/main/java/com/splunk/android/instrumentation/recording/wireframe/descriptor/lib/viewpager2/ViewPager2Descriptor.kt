package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.viewpager2

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class ViewPager2Descriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.viewpager2.widget.ViewPager2".toClass()
}
