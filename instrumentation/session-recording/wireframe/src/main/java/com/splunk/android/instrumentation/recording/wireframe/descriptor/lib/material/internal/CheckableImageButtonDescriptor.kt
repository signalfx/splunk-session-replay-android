package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatImageButtonDescriptor

internal open class CheckableImageButtonDescriptor : AppCompatImageButtonDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.internal.CheckableImageButton".toClass()
}
