package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

internal open class ActionBarContainerDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.ActionBarContainer".toClass()
}
