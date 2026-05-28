package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.swiperefreshlayout

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ImageViewDescriptor

internal open class CircleImageViewDescriptor : ImageViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.swiperefreshlayout.widget.CircleImageView".toClass()
}
