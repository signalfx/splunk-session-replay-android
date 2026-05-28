package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class ToolbarDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.Toolbar".toClass()
}
