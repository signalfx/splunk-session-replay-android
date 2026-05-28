package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.swiperefreshlayout

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class SwipeRefreshLayoutDescription : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.swiperefreshlayout.widget.SwipeRefreshLayout".toClass()
}
