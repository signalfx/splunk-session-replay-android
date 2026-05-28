package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal class RippleContainerDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.compose.material.ripple.RippleContainer".toClass()
}
