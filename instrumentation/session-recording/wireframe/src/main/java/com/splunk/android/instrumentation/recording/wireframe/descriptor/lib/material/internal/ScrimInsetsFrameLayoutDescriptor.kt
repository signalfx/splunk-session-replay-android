package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

internal open class ScrimInsetsFrameLayoutDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.internal.ScrimInsetsFrameLayout".toClass()
}
