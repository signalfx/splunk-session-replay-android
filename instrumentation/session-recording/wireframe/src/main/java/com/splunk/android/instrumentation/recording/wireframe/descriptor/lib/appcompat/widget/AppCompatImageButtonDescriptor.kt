package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ImageButtonDescriptor

internal open class AppCompatImageButtonDescriptor : ImageButtonDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatImageButton".toClass()
}
