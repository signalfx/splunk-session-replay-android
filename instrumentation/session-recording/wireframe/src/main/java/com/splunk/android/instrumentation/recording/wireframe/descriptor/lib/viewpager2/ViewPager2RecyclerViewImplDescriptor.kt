package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.viewpager2

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.recyclerview.RecyclerViewDescriptor

internal open class ViewPager2RecyclerViewImplDescriptor : RecyclerViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.viewpager2.widget.ViewPager2\$RecyclerViewImpl".toClass()
}
