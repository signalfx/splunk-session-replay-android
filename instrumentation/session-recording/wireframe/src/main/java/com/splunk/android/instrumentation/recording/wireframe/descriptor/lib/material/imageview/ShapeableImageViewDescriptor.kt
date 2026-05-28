package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.imageview

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatImageViewDescriptor

internal open class ShapeableImageViewDescriptor : AppCompatImageViewDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.imageview.ShapeableImageView".toClass()
}
