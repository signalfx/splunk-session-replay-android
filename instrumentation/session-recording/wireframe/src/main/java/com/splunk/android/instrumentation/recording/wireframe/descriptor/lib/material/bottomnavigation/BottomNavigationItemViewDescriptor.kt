package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.bottomnavigation

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

internal open class BottomNavigationItemViewDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.bottomnavigation.BottomNavigationItemView".toClass()
}
