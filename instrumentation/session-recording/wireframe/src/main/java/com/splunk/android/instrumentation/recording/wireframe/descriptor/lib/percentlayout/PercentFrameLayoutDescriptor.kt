package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.percentlayout

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

internal open class PercentFrameLayoutDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "androidx.percentlayout.widget.PercentFrameLayout".toClass()
}
