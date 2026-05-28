package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.flexbox

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class FlexboxLayoutDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.flexbox.FlexboxLayout".toClass()
}
