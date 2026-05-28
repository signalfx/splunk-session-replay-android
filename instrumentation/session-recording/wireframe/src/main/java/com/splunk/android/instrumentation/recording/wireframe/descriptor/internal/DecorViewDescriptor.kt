package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class DecorViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "com.android.internal.policy.DecorView".toClass()
}
