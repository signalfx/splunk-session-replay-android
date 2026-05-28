package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.fragment

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

internal open class FragmentContainerViewDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "androidx.fragment.app.FragmentContainerView".toClass()
}
