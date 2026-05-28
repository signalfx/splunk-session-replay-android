package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

internal class PhoneWindowDecorViewDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.android.internal.policy.impl.PhoneWindow\$DecorView".toClass()
}
