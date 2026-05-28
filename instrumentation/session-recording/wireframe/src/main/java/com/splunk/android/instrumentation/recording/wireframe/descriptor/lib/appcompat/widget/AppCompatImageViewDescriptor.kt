package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ImageViewDescriptor

internal open class AppCompatImageViewDescriptor : ImageViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatImageView".toClass()
}
